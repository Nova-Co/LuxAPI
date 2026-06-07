package com.novaco.luxapi.cobblemon.boss.reward

import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items

/**
 * Safely gives an item to a player. If their inventory is full, drops it on the ground.
 *
 * @param itemId The namespaced ID of the item (e.g., "minecraft:diamond", "cobblemon:rare_candy").
 * @param amount The quantity to give.
 */
fun ServerPlayer.giveItem(itemId: String, amount: Int = 1) {
    val location = try {
        ResourceLocation.parse(itemId)
    } catch (e: Exception) {
        return
    }

    val itemType = BuiltInRegistries.ITEM.get(location)

    if (itemType == Items.AIR && itemId != "minecraft:air") {
        println("[LuxAPI] Warning: Could not find item '$itemId' to give to ${this.name.string}")
        return
    }

    val itemStack = ItemStack(itemType, amount)

    if (!this.inventory.add(itemStack)) {
        this.drop(itemStack, false)
    }
}