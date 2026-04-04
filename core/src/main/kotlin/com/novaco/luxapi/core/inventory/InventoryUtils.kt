package com.novaco.luxapi.core.inventory

import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack

/**
 * A comprehensive utility class for safely managing player inventories.
 * Handles tasks such as checking capacity, adding/removing items,
 * and safely distributing rewards without item loss.
 */
object InventoryUtils {

    /**
     * Checks if the player has at least one completely empty slot in their main inventory.
     *
     * @param player The player to check.
     * @return True if there is at least one empty slot, false otherwise.
     */
    fun hasFreeSpace(player: Player): Boolean {
        return player.inventory.getFreeSlot() != -1
    }

    /**
     * Calculates the total number of completely empty slots in the player's main inventory.
     *
     * @param player The player to check.
     * @return The number of empty slots available.
     */
    fun countFreeSlots(player: Player): Int {
        var freeSlots = 0
        for (itemStack in player.inventory.items) {
            if (itemStack.isEmpty) {
                freeSlots++
            }
        }
        return freeSlots
    }

    /**
     * Completely clears the player's inventory, including their main inventory,
     * armor slots, and offhand slot.
     *
     * @param player The player whose inventory will be cleared.
     */
    fun clearInventory(player: Player) {
        player.inventory.clearContent()
    }

    /**
     * Safely gives an item to the player. If the player's inventory is full,
     * the remaining items will be naturally dropped on the ground at the player's location.
     *
     * @param player The player receiving the item.
     * @param itemStack The item stack to give.
     */
    fun giveItemSafely(player: Player, itemStack: ItemStack) {
        val addedSuccessfully = player.inventory.add(itemStack)

        if (!itemStack.isEmpty && !addedSuccessfully) {
            player.drop(itemStack, false)
        }
    }

    /**
     * Checks if the player has at least the specified amount of a certain item type.
     *
     * @param player The player to check.
     * @param item The specific item type to look for.
     * @param amount The minimum amount required.
     * @return True if the player has the required amount, false otherwise.
     */
    fun hasItemAmount(player: Player, item: Item, amount: Int): Boolean {
        var count = 0
        for (itemStack in player.inventory.items) {
            if (itemStack.`is`(item)) {
                count += itemStack.count
                if (count >= amount) return true
            }
        }
        return false
    }

    /**
     * Consumes a specific amount of an item from the player's inventory.
     * Note: You should check if the player has the required amount using [hasItemAmount]
     * before calling this method to prevent partial consumption.
     *
     * @param player The player whose items will be consumed.
     * @param item The specific item type to consume.
     * @param amount The total amount to remove.
     * @return True if the exact amount was successfully consumed, false otherwise.
     */
    fun consumeItem(player: Player, item: Item, amount: Int): Boolean {
        if (!hasItemAmount(player, item, amount)) return false

        var remainingToRemove = amount
        for (i in 0 until player.inventory.containerSize) {
            val itemStack = player.inventory.getItem(i)

            if (itemStack.`is`(item)) {
                if (itemStack.count <= remainingToRemove) {
                    remainingToRemove -= itemStack.count
                    player.inventory.setItem(i, ItemStack.EMPTY)
                } else {
                    itemStack.shrink(remainingToRemove)
                    remainingToRemove = 0
                }
            }

            if (remainingToRemove <= 0) break
        }
        return true
    }
}