package com.novaco.luxapi.core.item

import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Item

/**
 * Wraps the native per-item use cooldown (the same system behind ender pearls/chorus fruit).
 * Distinct from the arbitrary-key cooldowns in `commons`' CooldownManager.
 */
object PlayerCooldownUtils {

    fun isOnCooldown(player: Player, item: Item): Boolean {
        return player.cooldowns.isOnCooldown(item)
    }

    /**
     * Returns how far through its cooldown [item] is, from 1.0 (just used) to 0.0 (ready).
     */
    fun getCooldownPercent(player: Player, item: Item, partialTick: Float = 0f): Float {
        return player.cooldowns.getCooldownPercent(item, partialTick)
    }

    fun setCooldown(player: Player, item: Item, ticks: Int) {
        player.cooldowns.addCooldown(item, ticks)
    }

    fun clearCooldown(player: Player, item: Item) {
        player.cooldowns.removeCooldown(item)
    }
}
