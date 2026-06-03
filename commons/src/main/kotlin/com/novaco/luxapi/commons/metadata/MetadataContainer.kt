package com.novaco.luxapi.commons.metadata

import java.util.concurrent.ConcurrentHashMap

/**
 * Represents a thread-safe container capable of holding temporary metadata.
 * Metadata is stored as key-value pairs and is typically bound to a player's session.
 */
class MetadataContainer {

    private val dataMap = ConcurrentHashMap<String, Any>()

    /**
     * Sets a metadata value for the given key.
     *
     * @param key The unique identifier for the metadata.
     * @param value The data to store.
     */
    fun set(key: String, value: Any) {
        dataMap[key.lowercase()] = value
    }

    /**
     * Retrieves a metadata value safely cast to the specified class type.
     *
     * @param key The unique identifier for the metadata.
     * @param clazz The expected class type of the metadata.
     * @return The cast value, or null if the key doesn't exist or the type mismatches.
     */
    fun <T> get(key: String, clazz: Class<T>): T? {
        val value = dataMap[key.lowercase()] ?: return null
        return if (clazz.isInstance(value)) clazz.cast(value) else null
    }

    /**
     * Checks if metadata exists for the given key.
     *
     * @param key The unique identifier for the metadata.
     * @return True if the metadata exists, false otherwise.
     */
    fun has(key: String): Boolean {
        return dataMap.containsKey(key.lowercase())
    }

    /**
     * Removes the metadata associated with the given key.
     *
     * @param key The unique identifier for the metadata.
     */
    fun remove(key: String) {
        dataMap.remove(key.lowercase())
    }

    /**
     * Clears all metadata from this container.
     */
    fun clear() {
        dataMap.clear()
    }
}