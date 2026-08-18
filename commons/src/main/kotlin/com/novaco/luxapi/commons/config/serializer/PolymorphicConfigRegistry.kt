package com.novaco.luxapi.commons.config.serializer

import com.novaco.luxapi.commons.registry.Registry
import org.spongepowered.configurate.serialize.TypeSerializer

/**
 * Maps string discriminator ids to concrete [PolymorphicConfigEntry] implementations, and hands
 * out a [TypeSerializer] that resolves the right implementation from a config section's
 * discriminator field. One registry instance per base type/interface.
 *
 * Replaces the pattern EnvyWare/API hand-rolls separately for each discriminated section
 * (database backends, click actions, display rules) with a single reusable mechanism, backed
 * by [Registry] — the same key/value primitive already used elsewhere in this codebase.
 *
 * Register instances built with this must still be handed to [ConfigTypeSerializerRegistry]
 * (via [serializer]) before loading any config that references the base type — same requirement
 * as any other custom type serializer.
 */
class PolymorphicConfigRegistry<T : PolymorphicConfigEntry>(private val discriminatorField: String = "type") {

    private val idToType = Registry<String, Class<out T>>()

    /**
     * Registers [type] under [id]. [type] must be default-constructible (a no-arg constructor)
     * so Configurate's object mapper can instantiate it during deserialization.
     */
    fun register(id: String, type: Class<out T>) {
        idToType.register(id.lowercase(), type)
    }

    /**
     * The class registered for [id], or null if nothing is registered under it.
     */
    fun get(id: String): Class<out T>? = idToType.get(id.lowercase())

    /**
     * Builds the [TypeSerializer] for [baseType], to be registered against the base
     * interface/class (not each concrete implementation) via [ConfigTypeSerializerRegistry].
     */
    fun serializer(baseType: Class<T>): TypeSerializer<T> = PolymorphicTypeSerializer(baseType, this, discriminatorField)

    internal fun discriminatorField(): String = discriminatorField
}
