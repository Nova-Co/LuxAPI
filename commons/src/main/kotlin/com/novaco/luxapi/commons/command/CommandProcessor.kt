package com.novaco.luxapi.commons.command

import com.novaco.luxapi.commons.command.annotation.Command
import com.novaco.luxapi.commons.command.annotation.SubCommand
import com.novaco.luxapi.commons.command.annotation.TabComplete
import com.novaco.luxapi.commons.command.exception.CommandParseException
import com.novaco.luxapi.commons.command.injector.CompletingInjector
import com.novaco.luxapi.commons.command.injector.InjectorRegistry
import com.novaco.luxapi.commons.command.sender.CommandSender
import com.novaco.luxapi.commons.command.tab.TabHandler
import com.novaco.luxapi.commons.command.tab.TabRegistry
import com.novaco.luxapi.commons.player.LuxPlayer
import com.novaco.luxapi.commons.reflection.DirectInvoker
import org.slf4j.LoggerFactory
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import java.util.concurrent.ConcurrentHashMap

/**
 * The reflection-based execution engine that discovers, parses, and invokes command methods.
 * Automatically handles string argument injection and permission validation.
 *
 * @param commandInstance The object instance annotated with @Command.
 */
class CommandProcessor(private val commandInstance: Any) {

    private companion object {
        val logger = LoggerFactory.getLogger(CommandProcessor::class.java)
    }

    val subCommands = mutableMapOf<String, Method>()

    /**
     * One shared instance per @TabComplete handler class, reused across every
     * tab-completion request instead of reflecting a new instance per keystroke.
     * Safe because [TabHandler] implementations are required to be stateless.
     */
    private val tabHandlerCache = ConcurrentHashMap<Class<out TabHandler>, TabHandler>()

    val commandInfo: Command = commandInstance.javaClass.getAnnotation(Command::class.java)
        ?: throw IllegalArgumentException("Class ${commandInstance.javaClass.simpleName} is missing the @Command annotation.")

    private val mainExecuteMethods: List<Method> = findMainExecuteMethods()

    init {
        commandInstance.javaClass.declaredMethods.forEach { method ->
            method.getAnnotation(SubCommand::class.java)?.let { subAnnotation ->
                subCommands[subAnnotation.name.lowercase()] = method
                subAnnotation.aliases.forEach { alias ->
                    subCommands[alias.lowercase()] = method
                }
            }
        }
    }

    /**
     * Processes a raw command request and routes it to the appropriate method.
     *
     * @param sender The entity dispatching the command.
     * @param args The raw string arguments.
     */
    fun process(sender: CommandSender, args: Array<String>) {
        try {
            if (commandInfo.permission.isNotEmpty() && !sender.hasPermission(commandInfo.permission)) {
                throw CommandParseException("§cYou do not have permission to execute this command.")
            }

            if (args.isNotEmpty() && subCommands.containsKey(args[0].lowercase())) {
                val subMethod = subCommands[args[0].lowercase()]!!
                val subAnnotation = subMethod.getAnnotation(SubCommand::class.java)!!

                if (subAnnotation.permission.isNotEmpty() && !sender.hasPermission(subAnnotation.permission)) {
                    throw CommandParseException("§cYou do not have permission for this sub-command.")
                }

                val remainingArgs = args.drop(1).toTypedArray()
                val methodArgs = buildArgumentsForMethod(subMethod, sender, remainingArgs)
                DirectInvoker.invoke(subMethod, commandInstance, methodArgs)
                return
            }

            val targetMainMethod = mainExecuteMethods.firstOrNull { it.parameterCount - 1 == args.size }
                ?: mainExecuteMethods.first()

            val mainArgs = buildArgumentsForMethod(targetMainMethod, sender, args)
            DirectInvoker.invoke(targetMainMethod, commandInstance, mainArgs)

        } catch (e: InvocationTargetException) {
            val cause = e.cause
            if (cause is CommandParseException) {
                sender.sendMessage(cause.errorMessage)
            } else {
                logger.error("Unhandled exception while executing command '{}'", commandInfo.name, cause)
                sender.sendMessage("§cAn internal error occurred while executing this command.")
            }
        } catch (e: CommandParseException) {
            sender.sendMessage(e.errorMessage)
        } catch (e: IllegalAccessException) {
            logger.error("Unexpected exception while executing command '{}'", commandInfo.name, e)
            sender.sendMessage("§cAn unexpected error occurred.")
        } catch (e: IllegalStateException) {
            logger.error("Unexpected exception while executing command '{}'", commandInfo.name, e)
            sender.sendMessage("§cAn unexpected error occurred.")
        } catch (e: IllegalArgumentException) {
            logger.error("Unexpected exception while executing command '{}'", commandInfo.name, e)
            sender.sendMessage("§cAn unexpected error occurred.")
        }
    }

    /**
     * Dynamically compiles the argument array required to invoke the target method.
     *
     * @param method The target execution method.
     * @param sender The original command dispatcher.
     * @param args The raw command arguments.
     * @return An array of injected objects ready for reflective invocation.
     */
    private fun buildArgumentsForMethod(method: Method, sender: CommandSender, args: Array<String>): Array<Any?> {
        val parameters = method.parameterTypes
        val result = arrayOfNulls<Any>(parameters.size)

        if (parameters.isEmpty()) {
            throw IllegalStateException("Method ${method.name} must have at least one parameter (CommandSender or LuxPlayer).")
        }

        val senderParamType = parameters[0]
        if (senderParamType == LuxPlayer::class.java && sender !is LuxPlayer) {
            throw CommandParseException("§cThis command can only be executed by a player.")
        }
        if (!CommandSender::class.java.isAssignableFrom(senderParamType)) {
            throw IllegalStateException("The first parameter of ${method.name} must be a CommandSender or LuxPlayer.")
        }
        result[0] = sender

        var argIndex = 0
        for (i in 1 until parameters.size) {
            val paramType = wrapPrimitive(parameters[i])
            val injector = InjectorRegistry.getInjector(paramType as Class<Any>)
                ?: throw IllegalStateException("No ArgumentInjector found for type: ${paramType.simpleName}")
            result[i] = injector.instantiate(sender, args, argIndex)
            argIndex++
        }

        return result
    }

    /**
     * Evaluates and returns all valid main execution methods available in the command class.
     *
     * @return A list of methods sorted by parameter capacity in descending order.
     */
    private fun findMainExecuteMethods(): List<Method> {
        val methods = commandInstance.javaClass.declaredMethods.filter {
            java.lang.reflect.Modifier.isPublic(it.modifiers) &&
                    !it.isSynthetic &&
                    it.parameterCount >= 1 &&
                    CommandSender::class.java.isAssignableFrom(it.parameterTypes[0]) &&
                    !it.isAnnotationPresent(SubCommand::class.java)
        }
        if (methods.isEmpty()) {
            throw IllegalArgumentException("No valid main execute method found in ${commandInstance.javaClass.simpleName}.")
        }
        return methods.sortedByDescending { it.parameterCount }
    }

    /**
     * Contextually generates a list of tab completion suggestions for the provided input state.
     *
     * @param sender The entity requesting auto-completion.
     * @param args The current string array captured from the client.
     * @return A list of relevant suggestion strings.
     */
    fun getSuggestions(sender: CommandSender, args: Array<String>): List<String> {
        val currentInput = args.lastOrNull()?.lowercase() ?: ""

        if (args.size <= 1) {
            val suggestions = mutableListOf<String>()
            suggestions.addAll(subCommands.keys.filter { it.startsWith(currentInput) })
            mainExecuteMethods.forEach { method ->
                suggestions.addAll(getParameterSuggestions(method, sender, args))
            }
            return suggestions.distinct()
        }

        if (subCommands.containsKey(args[0].lowercase())) {
            val subMethod = subCommands[args[0].lowercase()]!!
            return getParameterSuggestions(subMethod, sender, args.drop(1).toTypedArray())
        }

        val targetMainMethod = mainExecuteMethods.firstOrNull { it.parameterCount - 1 >= args.size }
            ?: mainExecuteMethods.first()
        return getParameterSuggestions(targetMainMethod, sender, args)
    }

    /**
     * Retrieves contextual parameter suggestions utilizing the TabRegistry or explicit annotations.
     *
     * @param method The target execution method containing the parameters.
     * @param sender The entity requesting auto-completion.
     * @param args The current raw arguments.
     * @return A list of relevant suggestion strings.
     */
    private fun getParameterSuggestions(method: Method, sender: CommandSender, args: Array<String>): List<String> {
        val paramIndex = args.size
        val parameters = method.parameterTypes

        if (paramIndex >= parameters.size) return emptyList()

        val paramAnnotations = method.parameterAnnotations[paramIndex]
        paramAnnotations.filterIsInstance<TabComplete>().firstOrNull()?.let { tabAnnotation ->
            try {
                val handlerClass = tabAnnotation.value.java
                val handler = tabHandlerCache.computeIfAbsent(handlerClass) {
                    it.getDeclaredConstructor().newInstance()
                }
                return handler.getSuggestions(sender, args)
            } catch (e: Exception) {
                logger.error("Failed to resolve TabHandler '{}'", tabAnnotation.value.simpleName, e)
                return emptyList()
            }
        }

        val targetParam = wrapPrimitive(parameters[paramIndex])
        TabRegistry.getHandler(targetParam)?.let { handler ->
            return handler.getSuggestions(sender, args)
        }

        // Fallback: an injector that also knows how to complete its own type gets used
        // here, so command authors don't have to write a dedicated TabHandler for it.
        (InjectorRegistry.getInjector(targetParam) as? CompletingInjector<*>)?.let { injector ->
            return injector.getSuggestions(sender, args, args.size - 1)
        }

        return emptyList()
    }

    /**
     * Normalizes primitive class structures to their generic object wrappers.
     *
     * @param clazz The raw class structure.
     * @return The normalized class object.
     */
    private fun wrapPrimitive(clazz: Class<*>): Class<*> {
        return when (clazz) {
            Int::class.javaPrimitiveType -> Int::class.javaObjectType
            Boolean::class.javaPrimitiveType -> Boolean::class.javaObjectType
            Double::class.javaPrimitiveType -> Double::class.javaObjectType
            Float::class.javaPrimitiveType -> Float::class.javaObjectType
            else -> clazz
        }
    }
}