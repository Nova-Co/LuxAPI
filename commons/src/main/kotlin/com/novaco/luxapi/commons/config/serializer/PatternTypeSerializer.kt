package com.novaco.luxapi.commons.config.serializer

import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.serialize.SerializationException
import org.spongepowered.configurate.serialize.TypeSerializer
import java.lang.reflect.Type
import java.util.regex.Pattern
import java.util.regex.PatternSyntaxException

/**
 * (De)serializes a [Pattern] as its raw regex string. Not registered by default —
 * call `ConfigTypeSerializerRegistry.register(Pattern::class.java, PatternTypeSerializer)`
 * to opt in.
 */
object PatternTypeSerializer : TypeSerializer<Pattern> {

    override fun deserialize(type: Type, node: ConfigurationNode): Pattern {
        val raw = node.string ?: throw SerializationException(node, type, "Expected a regex string")
        return try {
            Pattern.compile(raw)
        } catch (e: PatternSyntaxException) {
            throw SerializationException(node, type, "Invalid regex pattern: ${e.message}", e)
        }
    }

    override fun serialize(type: Type, obj: Pattern?, node: ConfigurationNode) {
        if (obj == null) {
            node.set(null)
            return
        }
        node.set(obj.pattern())
    }
}
