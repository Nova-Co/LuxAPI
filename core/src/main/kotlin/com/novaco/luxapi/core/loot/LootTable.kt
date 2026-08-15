package com.novaco.luxapi.core.loot

import net.minecraft.world.item.ItemStack
import kotlin.random.Random

/**
 * A weighted pool of item entries. Each roll picks one entry proportional to its weight
 * and produces a stack with a random amount in [minAmount, maxAmount].
 */
class LootTable {

    private data class Entry(
        val item: () -> ItemStack,
        val weight: Int,
        val minAmount: Int,
        val maxAmount: Int
    )

    private val entries = mutableListOf<Entry>()

    /**
     * Adds a weighted entry. [item] is a factory so every roll gets a fresh ItemStack.
     */
    fun addEntry(weight: Int, minAmount: Int = 1, maxAmount: Int = minAmount, item: () -> ItemStack): LootTable {
        require(weight > 0) { "weight must be positive" }
        require(minAmount in 1..maxAmount) { "minAmount must be between 1 and maxAmount" }
        entries.add(Entry(item, weight, minAmount, maxAmount))
        return this
    }

    /**
     * Rolls a single item from the table, or null if the table has no entries.
     */
    fun roll(random: Random = Random): ItemStack? {
        if (entries.isEmpty()) return null

        val totalWeight = entries.sumOf { it.weight }
        var target = random.nextInt(totalWeight)
        val entry = entries.first { target -= it.weight; target < 0 }

        val amount = if (entry.minAmount == entry.maxAmount) {
            entry.minAmount
        } else {
            random.nextInt(entry.minAmount, entry.maxAmount + 1)
        }

        val stack = entry.item()
        stack.count = amount
        return stack
    }

    /**
     * Rolls [times] items from the table. Empty results (empty table) are omitted.
     */
    fun rollMultiple(times: Int, random: Random = Random): List<ItemStack> {
        return (0 until times).mapNotNull { roll(random) }
    }
}
