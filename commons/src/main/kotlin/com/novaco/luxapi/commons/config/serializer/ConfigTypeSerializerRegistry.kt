package com.novaco.luxapi.commons.config.serializer

import org.spongepowered.configurate.serialize.TypeSerializer

/**
 * Central registry for custom Configurate [TypeSerializer]s used by [com.novaco.luxapi.commons.config.ConfigService].
 * Consumers register a serializer once (e.g. on plugin init) so any config field of that type
 * is handled automatically across every load/save call.
 */
object ConfigTypeSerializerRegistry {

    private val registry = LinkedHashMap<Class<*>, TypeSerializer<*>>()

    fun <T : Any> register(type: Class<T>, serializer: TypeSerializer<T>) {
        registry[type] = serializer
    }

    fun get(type: Class<*>): TypeSerializer<*>? = registry[type]

    fun getAll(): Map<Class<*>, TypeSerializer<*>> = registry.toMap()
}
