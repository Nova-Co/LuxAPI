package com.novaco.luxapi.commons.config.serializer

import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.serialize.SerializationException
import org.spongepowered.configurate.serialize.TypeSerializer
import java.lang.reflect.Type

/**
 * Resolves a config section into the concrete [PolymorphicConfigEntry] implementation named
 * by its discriminator field, using the id-to-class mapping in [registry]. Built via
 * [PolymorphicConfigRegistry.serializer] — not constructed directly by callers.
 */
internal class PolymorphicTypeSerializer<T : PolymorphicConfigEntry>(
    private val baseType: Class<T>,
    private val registry: PolymorphicConfigRegistry<T>,
    private val discriminatorField: String
) : TypeSerializer<T> {

    override fun deserialize(type: Type, node: ConfigurationNode): T {
        val id = node.node(discriminatorField).string
            ?: throw SerializationException(node, type, "Missing discriminator field '$discriminatorField' for ${baseType.simpleName}")

        val concreteType = registry.get(id)
            ?: throw SerializationException(node, type, "No type registered for id '$id' under ${baseType.simpleName}")

        return node.get(concreteType)
            ?: throw SerializationException(node, type, "Failed to deserialize '$id' as ${concreteType.simpleName}")
    }

    override fun serialize(type: Type, obj: T?, node: ConfigurationNode) {
        if (obj == null) {
            node.set(null)
            return
        }

        @Suppress("UNCHECKED_CAST")
        node.set(obj::class.java as Class<Any>, obj)
        node.node(discriminatorField).set(obj.id())
    }
}
