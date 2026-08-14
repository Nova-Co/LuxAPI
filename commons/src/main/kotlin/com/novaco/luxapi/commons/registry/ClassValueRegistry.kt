package com.novaco.luxapi.commons.registry

/**
 * A lock-free, per-[Class] lazy cache backed by the JVM's own [ClassValue]. This is a
 * different tool from [Registry], not a drop-in replacement for it: [Registry] holds
 * values that are explicitly `register()`-ed up front, while this only ever holds a
 * value that [factory] can *compute purely from the Class itself* — there is no
 * `register()`, no enumeration, and no size. Reach for this when the value only
 * depends on the Class (e.g. caching a reflective lookup result keyed by type), and
 * only if profiling actually shows contention on a plain map — a `ConcurrentHashMap`
 * is simpler and correct for the vast majority of Class-keyed lookups in this codebase.
 *
 * @param factory Computes the value for a [Class] the first time it's requested.
 *   Called at most once per Class per JVM classloader, even under concurrent access.
 */
class ClassValueRegistry<V : Any>(factory: (Class<*>) -> V) {

    private val cache = object : ClassValue<V>() {
        override fun computeValue(type: Class<*>): V = factory(type)
    }

    /**
     * Returns the cached value for [clazz], computing and storing it via the
     * constructor's factory function if this is the first request for [clazz].
     */
    fun get(clazz: Class<*>): V = cache.get(clazz)

    /**
     * Drops the cached value for [clazz], if any. The next [get] call for [clazz]
     * recomputes it via the factory.
     */
    fun remove(clazz: Class<*>) {
        cache.remove(clazz)
    }
}
