package com.novaco.luxapi.commons.config.gui.action

import com.novaco.luxapi.commons.chat.placeholder.PlaceholderManager
import com.novaco.luxapi.commons.config.gui.ClickAction
import com.novaco.luxapi.commons.config.gui.MenuItemCooldowns
import com.novaco.luxapi.commons.gui.GuiClickEvent
import org.spongepowered.configurate.objectmapping.ConfigSerializable

/**
 * Gates whatever [ClickAction]s come after it in the same item's `click-actions` list behind a
 * cooldown — place this first in the list. Starts the cooldown and lets the rest of the list run
 * if [cooldownId] isn't active; otherwise sends [onCooldownMessage] (if set) and stops the list.
 * Registered under id `"cooldown"`.
 */
@ConfigSerializable
class CooldownClickAction : ClickAction {

    var cooldownId: String = ""
    var durationMillis: Long = 1000
    var onCooldownMessage: String = ""

    override fun id(): String = "cooldown"

    override fun handle(event: GuiClickEvent): Boolean {
        if (MenuItemCooldowns.isOnCooldown(event.player, cooldownId)) {
            if (onCooldownMessage.isNotBlank()) {
                event.player.sendMessage(PlaceholderManager.replace(event.player, onCooldownMessage))
            }
            return false
        }

        MenuItemCooldowns.start(event.player, cooldownId, durationMillis)
        return true
    }
}
