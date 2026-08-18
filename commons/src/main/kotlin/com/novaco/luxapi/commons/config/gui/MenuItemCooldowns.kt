package com.novaco.luxapi.commons.config.gui

import com.novaco.luxapi.commons.data.CooldownManager
import com.novaco.luxapi.commons.player.LuxPlayer
import java.util.UUID

/**
 * Player+cooldownId-keyed cooldowns shared by [action.CooldownClickAction] and
 * [rule.CooldownDisplayRule] — the `cooldownId` is a config-author-chosen bucket name, so
 * several items can intentionally share one cooldown (e.g. "any crate opening").
 */
object MenuItemCooldowns {

    private val manager = CooldownManager<Pair<UUID, String>>()

    fun start(player: LuxPlayer, cooldownId: String, durationMillis: Long) {
        manager.setCooldown(player.uniqueId to cooldownId, durationMillis)
    }

    fun isOnCooldown(player: LuxPlayer, cooldownId: String): Boolean =
        manager.isOnCooldown(player.uniqueId to cooldownId)

    fun getRemainingMillis(player: LuxPlayer, cooldownId: String): Long =
        manager.getRemainingTime(player.uniqueId to cooldownId)
}
