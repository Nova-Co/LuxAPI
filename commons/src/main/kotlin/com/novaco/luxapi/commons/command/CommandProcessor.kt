package com.novaco.luxapi.commons.command

import com.novaco.luxapi.commons.command.annotation.Command
import com.novaco.luxapi.commons.command.annotation.SubCommand
import com.novaco.luxapi.commons.command.exception.CommandParseException
import com.novaco.luxapi.commons.command.injector.InjectorRegistry
import com.novaco.luxapi.commons.command.sender.CommandSender
import com.novaco.luxapi.commons.player.LuxPlayer
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method

/**
 * The core engine that processes executed commands and sub-commands.
 * It dynamically maps string arguments to method parameters using injectors.
 */
class CommandProcessor(private val commandInstance: Any) {

    val subCommands = mutableMapOf<String, Method>()

    val commandInfo: Command = commandInstance.javaClass.getAnnotation(Command::class.java)
        ?: throw IllegalArgumentException("Class ${commandInstance.javaClass.simpleName} is missing the @Command annotation.")

    private val mainExecuteMethods: List<Method> = findMainExecuteMethods()

    init {
        commandInstance.javaClass.declaredMethods.forEach { method ->
            val subAnnotation = method.getAnnotation(SubCommand::class.java)
            if (subAnnotation != null) {
                subCommands[subAnnotation.name.lowercase()] = method
                subAnnotation.aliases.forEach { alias ->
                    subCommands[alias.lowercase()] = method
                }
            }
        }
    }

    /**
     * Entry point for command execution.
     * Decides whether to route to a sub-command or execute the main command logic.
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
                subMethod.invoke(commandInstance, *methodArgs)
                return
            }

            val targetMainMethod = mainExecuteMethods.firstOrNull { it.parameterCount - 1 == args.size }
                ?: mainExecuteMethods.first()

            val mainArgs = buildArgumentsForMethod(targetMainMethod, sender, args)
            targetMainMethod.invoke(commandInstance, *mainArgs)

        } catch (e: InvocationTargetException) {
            val cause = e.cause
            if (cause is CommandParseException) {
                sender.sendMessage(cause.errorMessage)
            } else {
                cause?.printStackTrace()
                sender.sendMessage("§cAn internal error occurred while executing this command.")
            }
        } catch (e: CommandParseException) {
            sender.sendMessage(e.errorMessage)
        } catch (e: Exception) {
            e.printStackTrace()
            sender.sendMessage("§cAn unexpected error occurred.")
        }
    }

    /**
     * Dynamically builds an array of objects to be passed as method arguments.
     * Uses InjectorRegistry to transform String arguments into the required types.
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
     * Locates all primary execution methods (not annotated with @SubCommand).
     * Sorts them descending by parameter count for fallback logic.
     */
    private fun findMainExecuteMethods(): List<Method> {
        val methods = commandInstance.javaClass.declaredMethods.filter {
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
     * Generates tab completion suggestions based on the current input and available online players.
     */
    fun getSuggestions(sender: CommandSender, args: Array<String>, onlinePlayers: List<String>): List<String> {
        val currentInput = args.lastOrNull()?.lowercase() ?: ""

        if (args.size <= 1) {
            val suggestions = mutableListOf<String>()
            suggestions.addAll(subCommands.keys.filter { it.startsWith(currentInput) })

            mainExecuteMethods.forEach { method ->
                suggestions.addAll(getParameterSuggestions(method, sender, args, onlinePlayers))
            }

            return suggestions.distinct()
        }

        if (subCommands.containsKey(args[0].lowercase())) {
            val subMethod = subCommands[args[0].lowercase()]!!
            return getParameterSuggestions(subMethod, sender, args.drop(1).toTypedArray(), onlinePlayers)
        }

        val targetMainMethod = mainExecuteMethods.firstOrNull { it.parameterCount - 1 >= args.size }
            ?: mainExecuteMethods.first()

        return getParameterSuggestions(targetMainMethod, sender, args, onlinePlayers)
    }

    /**
     * Returns suggestions for specific method parameters such as online players.
     */
    private fun getParameterSuggestions(method: Method, sender: CommandSender, args: Array<String>, onlinePlayers: List<String>): List<String> {
        val paramIndex = args.size
        val parameters = method.parameterTypes

        if (paramIndex >= parameters.size) return emptyList()

        val targetParam = wrapPrimitive(parameters[paramIndex])
        val currentInput = args.lastOrNull()?.lowercase() ?: ""

        if (targetParam == LuxPlayer::class.java) {
            return onlinePlayers.filter { it.lowercase().startsWith(currentInput) }
        }

        return emptyList()
    }

    /**
     * Ensures primitive types are treated as their Object counterparts for registry lookups.
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