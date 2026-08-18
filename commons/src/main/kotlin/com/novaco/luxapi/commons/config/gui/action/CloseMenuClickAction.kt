package com.novaco.luxapi.commons.config.gui.action

import com.novaco.luxapi.commons.config.gui.ClickAction
import com.novaco.luxapi.commons.gui.GuiClickEvent
import org.spongepowered.configurate.objectmapping.ConfigSerializable

/** Closes the menu for the clicking player. Registered under id `"close"`. */
@ConfigSerializable
class CloseMenuClickAction : ClickAction {

    override fun id(): String = "close"

    override fun handle(event: GuiClickEvent): Boolean {
        event.gui.close(event.player)
        return true
    }
}
