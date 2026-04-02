package com.novaco.luxapi.commons.command

import com.novaco.luxapi.commons.command.annotation.Command
import java.util.concurrent.ConcurrentHashMap

/**
 * The base implementation of the Command Manager.
 * It stores and organizes commands, waiting for platform-specific
 * modules (Fabric/NeoForge) to register them into the game engine.
 */
abstract class AbstractCommandManager : CommandManager {

    // Use a map to store root command names and their corresponding processors
    protected val registeredCommands = ConcurrentHashMap<String, CommandProcessor>()

    /**
     * Registers a command instance by wrapping it in a CommandProcessor.
     *
     * @param commandInstance An object annotated with @LuxCommand.
     */
    override fun register(commandInstance: Any) {
        val processor = CommandProcessor(commandInstance)
        val annotation = processor.commandInfo

        // Store the command using its primary name
        registeredCommands[annotation.name.lowercase()] = processor

        // Register the command to the actual Minecraft server (Platform dependent)
        registerToPlatform(processor)
    }

    /**
     * Abstract method to be implemented by Fabric/NeoForge modules.
     * This handles the actual registration into the Minecraft command dispatcher.
     *
     * @param processor The prepared CommandProcessor containing all logic.
     */
    abstract fun registerToPlatform(processor: CommandProcessor)

    /**
     * Retrieves a processor for a specific command name.
     * * @param name The command name (e.g., "luxapi").
     * @return The CommandProcessor if found, otherwise null.
     */
    fun getCommand(name: String): CommandProcessor? {
        return registeredCommands[name.lowercase()]
    }

    /**
     * Returns all registered command names.
     */
    fun getRegisteredCommandNames(): Set<String> {
        return registeredCommands.keys
    }
}