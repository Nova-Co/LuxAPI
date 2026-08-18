package com.novaco.luxapi.commons.config.util

import org.slf4j.LoggerFactory
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.serialize.SerializationException

/**
 * Small helpers for reading Configurate nodes gracefully instead of letting a single malformed
 * entry blow up an entire config load.
 */
object ConfigUtils {

    private val logger = LoggerFactory.getLogger(ConfigUtils::class.java)

    /**
     * Deserializes the node at [path] as a `List<T>`, logging and returning an empty list
     * instead of throwing if the data at that path doesn't match [type].
     */
    fun <T> getList(node: ConfigurationNode, type: Class<T>, vararg path: Any): List<T> {
        return try {
            node.node(*path).getList(type) ?: emptyList()
        } catch (e: SerializationException) {
            logger.warn("Failed to deserialize list of {} at path {}: {}", type.simpleName, path.joinToString("."), e.message)
            emptyList()
        }
    }
}
