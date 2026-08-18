package com.novaco.luxapi.commons.config.gui.action

import com.novaco.luxapi.commons.config.gui.ClickAction
import com.novaco.luxapi.commons.config.gui.MenuNavigator
import com.novaco.luxapi.commons.gui.GuiClickEvent
import org.spongepowered.configurate.objectmapping.ConfigSerializable

/** Opens another registered menu (see [com.novaco.luxapi.commons.config.gui.MenuRegistry]). Registered under id `"open_menu"`. */
@ConfigSerializable
class OpenMenuClickAction : ClickAction {

    var menuId: String = ""

    override fun id(): String = "open_menu"

    override fun handle(event: GuiClickEvent): Boolean {
        MenuNavigator.open(event.player, menuId)
        return true
    }
}
