package com.novaco.luxapi.commons.command

import com.novaco.luxapi.commons.command.annotation.Command
import com.novaco.luxapi.commons.command.annotation.SubCommand
import com.novaco.luxapi.commons.command.annotation.TabComplete
import com.novaco.luxapi.commons.command.exception.CommandParseException
import com.novaco.luxapi.commons.command.injector.InjectorRegistry
import com.novaco.luxapi.commons.command.sender.CommandSender
import com.novaco.luxapi.commons.command.tab.TabRegistry
import com.novaco.luxapi.commons.player.LuxPlayer
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method

/**
 * The core engine that discovers, parses, and executes commands defined by annotations.
 * It dynamically maps string arguments from a command line to the parameters of a Java/Kotlin method
 * using a system of [ArgumentInjector]s.
 *
 * @param commandInstance An instance of a class annotated with [Command].
 */
class CommandProcessor(private val commandInstance: Any) {

    /** A map of all discovered sub-commands, mapping their names and aliases to their respective [Method]. */
    val subCommands = mutableMapOf<String, Method>()

    /** The metadata for the main command, extracted from the [Command] annotation. */
    val commandInfo: Command = commandInstance.javaClass.getAnnotation(Command::class.java)
        ?: throw IllegalArgumentException("Class ${commandInstance.javaClass.simpleName} is missing the @Command annotation.")

    /** A list of all primary "execute" methods (those not marked as sub-commands). */
    private val mainExecuteMethods: List<Method> = findMainExecuteMethods()

    init {
        // Discover all methods annotated with @SubCommand and register them.
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
     * The main entry point for processing an executed command.
     * It validates permissions, determines whether to route to a sub-command or a main execution method,
     * and handles any exceptions that occur during parsing or execution.
     *
     * @param sender The entity that executed the command.
     * @param args The string arguments provided with the command.
     */
    fun process(sender: CommandSender, args: Array<String>) {
        try {
            // Check main command permission.
            if (commandInfo.permission.isNotEmpty() && !sender.hasPermission(commandInfo.permission)) {
                throw CommandParseException("§cYou do not have permission to execute this command.")
            }

            // Route to a sub-command if the first argument matches.
            if (args.isNotEmpty() && subCommands.containsKey(args[0].lowercase())) {
                val subMethod = subCommands[args[0].lowercase()]!!
                val subAnnotation = subMethod.getAnnotation(SubCommand::class.java)!!

                // Check sub-command permission.
                if (subAnnotation.permission.isNotEmpty() && !sender.hasPermission(subAnnotation.permission)) {
                    throw CommandParseException("§cYou do not have permission for this sub-command.")
                }

                val remainingArgs = args.drop(1).toTypedArray()
                val methodArgs = buildArgumentsForMethod(subMethod, sender, remainingArgs)
                subMethod.invoke(commandInstance, *methodArgs)
                return
            }

            // Otherwise, find the best matching main execution method based on argument count.
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
     * Dynamically constructs the argument array needed to invoke a method via reflection.
     * It uses the [InjectorRegistry] to convert string arguments into the required parameter types (e.g., String to Int).
     *
     * @param method The target method to build arguments for.
     * @param sender The command sender.
     * @param args The raw string arguments.
     * @return An array of instantiated objects ready to be passed to `method.invoke()`.
     */
    private fun buildArgumentsForMethod(method: Method, sender: CommandSender, args: Array<String>): Array<Any?> {
        val parameters = method.parameterTypes
        val result = arrayOfNulls<Any>(parameters.size)

        if (parameters.isEmpty()) {
            throw IllegalStateException("Method ${method.name} must have at least one parameter (CommandSender or LuxPlayer).")
        }

        // The first parameter must always be the sender.
        val senderParamType = parameters[0]
        if (senderParamType == LuxPlayer::class.java && sender !is LuxPlayer) {
            throw CommandParseException("§cThis command can only be executed by a player.")
        }
        if (!CommandSender::class.java.isAssignableFrom(senderParamType)) {
            throw IllegalStateException("The first parameter of ${method.name} must be a CommandSender or LuxPlayer.")
        }
        result[0] = sender

        // Process the remaining parameters using injectors.
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
     * Finds all valid main execution methods within the command class.
     * A valid method is one that is not a sub-command and has at least one parameter for the sender.
     *
     * @return A list of methods, sorted by parameter count in descending order.
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
     * Generates a list of tab completion suggestions for the current command input.
     * It provides suggestions for sub-commands and uses the [TabRegistry] for dynamic, type-based suggestions.
     *
     * @param sender The command sender requesting suggestions.
     * @param args The current arguments typed so far.
     * @return A list of suggested strings.
     */
    fun getSuggestions(sender: CommandSender, args: Array<String>): List<String> {
        val currentInput = args.lastOrNull()?.lowercase() ?: ""

        // For the first argument, suggest sub-commands and parameters of the main command.
        if (args.size <= 1) {
            val suggestions = mutableListOf<String>()
            suggestions.addAll(subCommands.keys.filter { it.startsWith(currentInput) })
            mainExecuteMethods.forEach { method ->
                suggestions.addAll(getParameterSuggestions(method, sender, args))
            }
            return suggestions.distinct()
        }

        // If a sub-command is being typed, provide suggestions for its parameters.
        if (subCommands.containsKey(args[0].lowercase())) {
            val subMethod = subCommands[args[0].lowercase()]!!
            return getParameterSuggestions(subMethod, sender, args.drop(1).toTypedArray())
        }

        // Otherwise, provide suggestions for the main command's parameters.
        val targetMainMethod = mainExecuteMethods.firstOrNull { it.parameterCount - 1 >= args.size }
            ?: mainExecuteMethods.first()
        return getParameterSuggestions(targetMainMethod, sender, args)
    }

    /**
     * Provides tab completion suggestions for a specific method's parameters.
     * It checks for [TabComplete] annotations or uses the default handler from the [TabRegistry].
     *
     * @param method The method to get parameter suggestions for.
     * @param sender The command sender.
     * @param args The current arguments.
     * @return A list of suggested strings.
     */
    private fun getParameterSuggestions(method: Method, sender: CommandSender, args: Array<String>): List<String> {
        val paramIndex = args.size
        val parameters = method.parameterTypes

        if (paramIndex >= parameters.size) return emptyList()

        // Check for a custom @TabComplete annotation on the parameter.
        val paramAnnotations = method.parameterAnnotations[paramIndex]
        paramAnnotations.filterIsInstance<TabComplete>().firstOrNull()?.let { tabAnnotation ->
            try {
                val handler = tabAnnotation.value.java.getDeclaredConstructor().newInstance()
                return handler.getSuggestions(sender, args)
            } catch (e: Exception) {
                e.printStackTrace()
                return emptyList()
            }
        }

        // Use the default handler for the parameter's type.
        val targetParam = wrapPrimitive(parameters[paramIndex])
        TabRegistry.getHandler(targetParam)?.let { handler ->
            return handler.getSuggestions(sender, args)
        }

        // Special case for LuxPlayer.
        if (targetParam == LuxPlayer::class.java) {
            TabRegistry.getHandler(LuxPlayer::class.java)?.let { luxHandler ->
                return luxHandler.getSuggestions(sender, args)
            }
        }

        return emptyList()
    }

    /**
     * Converts a primitive class type (e.g., `int.class`) to its corresponding wrapper class (`Integer.class`).
     * This is necessary for consistent lookups in registries that use class types as keys.
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