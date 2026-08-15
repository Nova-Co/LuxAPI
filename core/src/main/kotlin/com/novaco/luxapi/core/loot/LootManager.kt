package com.novaco.luxapi.core.loot

import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.item.ItemStack
import org.slf4j.LoggerFactory

/**
 * Centralized registry of weighted [LootTable]s, plus safe item delivery.
 */
object LootManager {

    private val logger = LoggerFactory.getLogger(LootManager::class.java)
    private val tables = mutableMapOf<String, LootTable>()

    /**
     * Registers a loot table under a specific ID, overwriting any existing table with that ID.
     */
    fun registerTable(lootId: String, table: LootTable) {
        tables[lootId] = table
    }

    /**
     * Returns the loot table registered under [lootId], or null if none is registered.
     */
    fun getTable(lootId: String): LootTable? = tables[lootId]

    /**
     * Rolls the table registered under [lootId] once per player and safely delivers the result.
     * No-ops with a warning if [lootId] isn't registered.
     */
    fun distribute(lootId: String, players: List<ServerPlayer>) {
        val table = tables[lootId]
        if (table == null) {
            logger.warn("LootManager.distribute() called with unregistered lootId '{}' — no-op.", lootId)
            return
        }
        players.forEach { player ->
            table.roll()?.let { safeGiveItem(player, it) }
        }
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
