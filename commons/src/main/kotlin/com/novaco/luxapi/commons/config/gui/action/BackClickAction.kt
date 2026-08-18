package com.novaco.luxapi.commons.config.gui.action

import com.novaco.luxapi.commons.config.gui.ClickAction
import com.novaco.luxapi.commons.config.gui.MenuNavigator
import com.novaco.luxapi.commons.gui.GuiClickEvent
import org.spongepowered.configurate.objectmapping.ConfigSerializable

/** Returns to whatever menu the player was on before this one. Registered under id `"back"`. */
@ConfigSerializable
class BackClickAction : ClickAction {

    override fun id(): String = "back"

    override fun handle(event: GuiClickEvent): Boolean {
        MenuNavigator.back(event.player)
        return true
    }
}
