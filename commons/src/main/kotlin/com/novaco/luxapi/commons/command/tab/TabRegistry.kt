package com.novaco.luxapi.commons.command.tab

/**
 * A centralized registry to store and retrieve TabHandlers for specific data types.
 * Allows modules to dynamically inject auto-completion logic into the command system.
 */
object TabRegistry {

    private val handlers = mutableMapOf<Class<*>, TabHandler>()

    /**
     * Registers a new TabHandler for a specific class type.
     *
     * @param clazz The class type representing the argument (e.g., Player::class.java).
     * @param handler The implementation providing the suggestions.
     */
    fun register(clazz: Class<*>, handler: TabHandler) {
        handlers[clazz] = handler
    }

    /**
     * Retrieves the registered TabHandler for a specific class type.
     *
     * @param clazz The class type to look up.
     * @return The corresponding TabHandler, or null if none is registered.
     */
    fun getHandler(clazz: Class<*>): TabHandler? {
        return handlers[clazz]
    }
}