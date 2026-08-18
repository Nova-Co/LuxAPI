package com.novaco.luxapi.commons.config.gui.action

import com.novaco.luxapi.commons.chat.placeholder.PlaceholderManager
import com.novaco.luxapi.commons.config.gui.ClickAction
import com.novaco.luxapi.commons.gui.GuiClickEvent
import org.spongepowered.configurate.objectmapping.ConfigSerializable

/** Sends a placeholder-resolved chat message to the clicking player. Registered under id `"message"`. */
@ConfigSerializable
class SendMessageClickAction : ClickAction {

    var message: String = ""

    override fun id(): String = "message"

    override fun handle(event: GuiClickEvent): Boolean {
        if (message.isNotBlank()) {
            event.player.sendMessage(PlaceholderManager.replace(event.player, message))
        }
        return true
    }
}
