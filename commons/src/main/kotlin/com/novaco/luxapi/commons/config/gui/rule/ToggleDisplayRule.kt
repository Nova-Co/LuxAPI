package com.novaco.luxapi.commons.config.gui.rule

import com.novaco.luxapi.commons.chat.placeholder.PlaceholderManager
import com.novaco.luxapi.commons.config.gui.ConfigMenuItem
import com.novaco.luxapi.commons.config.gui.ItemDisplayRule
import com.novaco.luxapi.commons.player.LuxPlayer
import org.spongepowered.configurate.objectmapping.ConfigSerializable

/**
 * Picks [trueItem] or [falseItem] based on [placeholder] resolving to `"true"` (case-insensitive)
 * for the viewer — an on/off switch item, e.g. reflecting a setting's current state. Falls back
 * to [current] on whichever side has no item configured. Registered under id `"toggle"`.
 */
@ConfigSerializable
class ToggleDisplayRule : ItemDisplayRule {

    var placeholder: String = ""
    var trueItem: ConfigMenuItem? = null
    var falseItem: ConfigMenuItem? = null

    override fun id(): String = "toggle"

    override fun resolve(player: LuxPlayer, current: ConfigMenuItem): ConfigMenuItem? {
        val truthy = PlaceholderManager.replace(player, placeholder).equals("true", ignoreCase = true)
        return if (truthy) (trueItem ?: current) else (falseItem ?: current)
    }
}
