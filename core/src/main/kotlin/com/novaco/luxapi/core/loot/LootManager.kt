package com.novaco.luxapi.core.loot

import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.item.ItemStack

/**
 * Centralized manager for handling reward distributions.
 * Provides safe item delivery and customizable loot tables via string IDs.
 */
object LootManager {

    private val lootTables = mutableMapOf<String, (List<ServerPlayer>) -> Unit>()

    /**
     * Registers a custom loot distribution logic under a specific ID.
     */
    fun registerLoot(lootId: String, distributionLogic: (List<ServerPlayer>) -> Unit) {
        lootTables[lootId] = distributionLogic
    }

    /**
     * Executes the distribution logic for a specific loot ID.
     */
    fun distribute(lootId: String, players: List<ServerPlayer>) {
        val logic = lootTables[lootId]
        logic?.invoke(players)
    }

    /**
     * Safely gives an item to a player.
     * Drops the item directly at the player's feet if their inventory is full.
     */
    fun safeGiveItem(player: ServerPlayer, itemStack: ItemStack) {
        val added = player.inventory.add(itemStack)
        if (!added || !itemStack.isEmpty) {
            val droppedItem = player.drop(itemStack, false)
            droppedItem?.setNoPickUpDelay()
            droppedItem?.setTarget(player.uuid)
        }
    }
}