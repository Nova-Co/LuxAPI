package com.novaco.luxapi.commons.registry

import java.util.concurrent.ConcurrentHashMap

/**
 * A generic, thread-safe key/value registry — the reusable primitive behind the shape
 * that `ServiceManager`, `TabRegistry`, and `InjectorRegistry` each already hand-roll
 * for their own specific key/value types. Those three are left as-is (each carries its
 * own domain-specific convenience methods); this is for new registries that don't need one.
 */
class Registry<K : Any, V : Any> {

    private val entries = ConcurrentHashMap<K, V>()

    /**
     * Registers [value] under [key], overwriting any existing entry.
     */
    fun register(key: K, value: V) {
        entries[key] = value
    }

    /**
     * Retrieves the value registered under [key], or null if none exists.
     */
    fun get(key: K): V? = entries[key]

    /**
     * Checks whether [key] currently has a registered value.
     */
    fun has(key: K): Boolean = entries.containsKey(key)

    /**
     * Removes the entry registered under [key], if any.
     */
    fun unregister(key: K) {
        entries.remove(key)
    }

    /**
     * Returns a read-only snapshot of every currently registered entry.
     */
    fun all(): Map<K, V> = entries.toMap()

    /**
     * The number of currently registered entries.
     */
    fun size(): Int = entries.size

    /**
     * Clears every entry from this registry.
     */
    fun clear() {
        entries.clear()
    }
}
