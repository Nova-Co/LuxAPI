package com.novaco.luxapi.commons.math

import java.util.concurrent.ConcurrentHashMap
import kotlin.random.Random

/**
 * A data structure that allows for the randomized selection of elements based on their assigned weights.
 * The higher the weight of an element compared to the total weight, the higher its chance of being selected.
 * This class is thread-safe, allowing safe reads and writes across asynchronous tasks.
 *
 * @param T The type of the objects stored in this set.
 */
class RandomWeightedSet<T> {

    private val entries = ConcurrentHashMap<T, Double>()

    /**
     * Adds an item to the weighted set.
     * If the item already exists, its weight will be overwritten.
     *
     * @param item The object to add.
     * @param weight The probability weight of the object (must be greater than 0).
     */
    fun add(item: T, weight: Double) {
        require(weight > 0) { "Weight must be greater than 0." }
        entries[item] = weight
    }

    /**
     * Removes an item from the weighted set.
     *
     * @param item The object to remove.
     */
    fun remove(item: T) {
        entries.remove(item)
    }

    /**
     * Clears all items and weights from the set.
     */
    fun clear() {
        entries.clear()
    }

    /**
     * Calculates the sum of all weights currently in the set.
     *
     * @return The total weight.
     */
    val totalWeight: Double
        get() = entries.values.sum()

    /**
     * Checks if the set contains any items.
     *
     * @return True if the set is empty, false otherwise.
     */
    val isEmpty: Boolean
        get() = entries.isEmpty()

    /**
     * Selects a random item from the set based on their assigned weights.
     *
     * @return A randomly selected item, or null if the set is empty.
     */
    fun getRandom(): T? {
        if (entries.isEmpty()) return null

        val total = totalWeight
        if (total <= 0.0) return null

        var randomValue = Random.nextDouble() * total

        for ((item, weight) in entries) {
            randomValue -= weight
            if (randomValue <= 0) {
                return item
            }
        }

        return entries.keys.firstOrNull()
    }

    /**
     * Returns a map of all items and their exact percentage chance of being selected.
     * Useful for displaying drop rates to players in a GUI or chat.
     *
     * @return A map containing the items and their percentage chance (0.0 to 100.0).
     */
    fun getProbabilities(): Map<T, Double> {
        val total = totalWeight
        if (total <= 0.0) return emptyMap()

        return entries.mapValues { (_, weight) ->
            (weight / total) * 100.0
        }
    }
}