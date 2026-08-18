package com.novaco.luxapi.commons.config.gui.rule

import com.novaco.luxapi.commons.chat.placeholder.PlaceholderManager
import com.novaco.luxapi.commons.config.gui.ConfigMenuItem
import com.novaco.luxapi.commons.config.gui.ItemDisplayRule
import com.novaco.luxapi.commons.player.LuxPlayer
import org.spongepowered.configurate.objectmapping.ConfigSerializable

/**
 * Shows [equals] compared against [placeholder] resolved for the viewer — swaps to [elseItem]
 * (or hides, if unset) on a mismatch. Lets server owners gate on their own plugin's state via a
 * placeholder instead of writing Kotlin. Registered under id `"placeholder_equals"`.
 */
@ConfigSerializable
class PlaceholderConditionDisplayRule : ItemDisplayRule {

    var placeholder: String = ""
    var equals: String = ""
    var elseItem: ConfigMenuItem? = null

    override fun id(): String = "placeholder_equals"

    override fun resolve(player: LuxPlayer, current: ConfigMenuItem): ConfigMenuItem? {
        val resolved = PlaceholderManager.replace(player, placeholder)
        return if (resolved == equals) current else elseItem
    }
}
