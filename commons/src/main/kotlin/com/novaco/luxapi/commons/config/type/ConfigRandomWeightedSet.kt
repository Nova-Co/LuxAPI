package com.novaco.luxapi.commons.config.type

import com.novaco.luxapi.commons.math.RandomWeightedSet
import org.spongepowered.configurate.objectmapping.ConfigSerializable

/**
 * A config-serializable list of weighted entries, backed by [RandomWeightedSet] for selection.
 * Rebuilds the underlying [RandomWeightedSet] fresh on every [getRandom] call instead of caching
 * it — [entries] can be overwritten in place by [com.novaco.luxapi.commons.config.LuxConfig.reload],
 * so a cached set would silently go stale after a reload.
 */
@ConfigSerializable
class ConfigRandomWeightedSet<T : Any> {

    @ConfigSerializable
    class WeightedEntry<T> {
        var value: T? = null
        var weight: Double = 1.0
    }

    var entries: MutableList<WeightedEntry<T>> = mutableListOf()

    fun add(value: T, weight: Double) {
        entries.add(WeightedEntry<T>().apply {
            this.value = value
            this.weight = weight
        })
    }

    fun getRandom(): T? {
        val set = RandomWeightedSet<T>()
        entries.forEach { entry -> entry.value?.let { set.add(it, entry.weight) } }
        return set.getRandom()
    }

    val isEmpty: Boolean
        get() = entries.isEmpty()
}
