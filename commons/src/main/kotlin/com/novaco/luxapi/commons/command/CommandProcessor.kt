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

    private val subCommands = mutableMapOf<String, Method>()

    val commandInfo: Command = commandInstance.javaClass.getAnnotation(Command::class.java)
        ?: throw IllegalArgumentException("Class ${commandInstance.javaClass.simpleName} is missing the @Command annotation.")

    private val mainExecuteMethod: Method = findMainExecuteMethod()

    init {
        // Scan the class for methods annotated with @SubCommand and index them
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
            // 1. Validate root command permissions
            if (commandInfo.permission.isNotEmpty() && !sender.hasPermission(commandInfo.permission)) {
                throw CommandParseException("§cYou do not have permission to execute this command.")
            }

            // 2. Route to Sub-Command if the first argument matches a registered sub-command
            if (args.isNotEmpty() && subCommands.containsKey(args[0].lowercase())) {
                val subMethod = subCommands[args[0].lowercase()]!!
                val subAnnotation = subMethod.getAnnotation(SubCommand::class.java)!!

                // Check sub-command specific permission
                if (subAnnotation.permission.isNotEmpty() && !sender.hasPermission(subAnnotation.permission)) {
                    throw CommandParseException("§cYou do not have permission for this sub-command.")
                }

                // Drop the first argument (the sub-command name) and process remaining args
                val remainingArgs = args.drop(1).toTypedArray()
                val methodArgs = buildArgumentsForMethod(subMethod, sender, remainingArgs)
                subMethod.invoke(commandInstance, *methodArgs)
                return
            }

            // 3. If no sub-command matched, execute the main command logic
            val mainArgs = buildArgumentsForMethod(mainExecuteMethod, sender, args)
            mainExecuteMethod.invoke(commandInstance, *mainArgs)

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

        // The first parameter must always be the sender
        val senderParamType = parameters[0]
        if (senderParamType == LuxPlayer::class.java && sender !is LuxPlayer) {
            throw CommandParseException("§cThis command can only be executed by a player.")
        }
        if (!CommandSender::class.java.isAssignableFrom(senderParamType)) {
            throw IllegalStateException("The first parameter of ${method.name} must be a CommandSender or LuxPlayer.")
        }
        result[0] = sender

        // Process following parameters using injectors
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
     * Locates the primary execution method (not annotated with @SubCommand).
     */
    private fun findMainExecuteMethod(): Method {
        return commandInstance.javaClass.declaredMethods.firstOrNull {
            it.parameterCount >= 1 &&
                    CommandSender::class.java.isAssignableFrom(it.parameterTypes[0]) &&
                    !it.isAnnotationPresent(SubCommand::class.java)
        } ?: throw IllegalArgumentException("No valid main execute method found in ${commandInstance.javaClass.simpleName}.")
    }

    /**
     * Generates tab completion suggestions based on the current input.
     */
    fun getSuggestions(sender: CommandSender, args: Array<String>): List<String> {
        // 1. Suggest Sub-commands if we are at the first argument
        if (args.size <= 1) {
            val currentInput = args.getOrNull(0)?.lowercase() ?: ""
            return subCommands.keys
                .filter { it.startsWith(currentInput) } // Suggest only matches
                .filter { name ->
                    // Check sub-command permission before suggesting
                    val method = subCommands[name]!!
                    val subAnnotation = method.getAnnotation(SubCommand::class.java)!!
                    subAnnotation.permission.isEmpty() || sender.hasPermission(subAnnotation.permission)
                }
        }

        // 2. If the first argument is a Sub-command, suggest for its parameters
        if (subCommands.containsKey(args[0].lowercase())) {
            val subMethod = subCommands[args[0].lowercase()]!!
            val remainingArgs = args.drop(1).toTypedArray()
            return getParameterSuggestions(subMethod, sender, remainingArgs)
        }

        // 3. Otherwise, suggest for the Main Command parameters
        return getParameterSuggestions(mainExecuteMethod, sender, args)
    }

    private fun getParameterSuggestions(method: Method, sender: CommandSender, args: Array<String>): List<String> {
        val paramIndex = args.size // Current index we are suggesting for
        val parameters = method.parameterTypes

        // Ensure the index is within the bounds of the method parameters (skipping the first sender parameter)
        if (paramIndex >= parameters.size) return emptyList()

        val targetParam = wrapPrimitive(parameters[paramIndex])
        val currentInput = args.lastOrNull()?.lowercase() ?: ""

        // In a full implementation, you could fetch specific TabHandlers from a registry here.
        // For now, we can provide basic suggestions for Players if the type is LuxPlayer.
        if (targetParam == LuxPlayer::class.java) {
            // This is a placeholder; in the platform module, you'll provide the actual player list.
            return emptyList()
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