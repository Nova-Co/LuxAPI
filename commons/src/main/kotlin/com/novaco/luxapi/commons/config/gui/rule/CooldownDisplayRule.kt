package com.novaco.luxapi.commons.config.gui.rule

import com.novaco.luxapi.commons.config.gui.ConfigMenuItem
import com.novaco.luxapi.commons.config.gui.ItemDisplayRule
import com.novaco.luxapi.commons.config.gui.MenuItemCooldowns
import com.novaco.luxapi.commons.player.LuxPlayer
import org.spongepowered.configurate.objectmapping.ConfigSerializable

/**
 * Shows [onCooldownItem] (or hides the item, if [hideWhileOnCooldown]) while [cooldownId] is
 * active for the viewer — pairs naturally with [com.novaco.luxapi.commons.config.gui.action.CooldownClickAction]
 * using the same [cooldownId]. Registered under id `"cooldown"`.
 */
@ConfigSerializable
class CooldownDisplayRule : ItemDisplayRule {

    var cooldownId: String = ""
    var hideWhileOnCooldown: Boolean = false
    var onCooldownItem: ConfigMenuItem? = null

    override fun id(): String = "cooldown"

    override fun resolve(player: LuxPlayer, current: ConfigMenuItem): ConfigMenuItem? {
        if (!MenuItemCooldowns.isOnCooldown(player, cooldownId)) return current
        return if (hideWhileOnCooldown) null else (onCooldownItem ?: current)
    }
}
