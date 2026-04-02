package com.novaco.luxapi.commons.command

/**
 * The core manager responsible for registering and parsing commands.
 * Platform-specific modules (like fabric20 or forge20) will implement this.
 */
interface CommandManager {

    /**
     * Registers a command class into the server.
     *
     * @param commandInstance The instance of the class annotated with @LuxCommand.
     */
    fun register(commandInstance: Any)

    /**
     * Registers multiple command classes at once.
     *
     * @param commandInstances Vararg of command instances.
     */
    fun registerAll(vararg commandInstances: Any) {
        for (instance in commandInstances) {
            register(instance)
        }
    }
}