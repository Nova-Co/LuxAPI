package com.novaco.luxapi.commons.command.tab

/**
 * A registry to store and retrieve TabHandlers for specific data types.
 */
object TabRegistry {
    private val handlers = mutableMapOf<Class<*>, TabHandler>()

    fun register(clazz: Class<*>, handler: TabHandler) {
        handlers[clazz] = handler
    }

    fun getHandler(clazz: Class<*>): TabHandler? = handlers[clazz]
}