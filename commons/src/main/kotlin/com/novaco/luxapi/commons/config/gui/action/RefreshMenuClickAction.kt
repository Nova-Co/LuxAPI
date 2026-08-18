package com.novaco.luxapi.commons.config.gui.action

import com.novaco.luxapi.commons.config.gui.ClickAction
import com.novaco.luxapi.commons.config.gui.MenuNavigator
import com.novaco.luxapi.commons.gui.GuiClickEvent
import org.spongepowered.configurate.objectmapping.ConfigSerializable

/**
 * Re-shows the player's current menu, re-resolving display rules and item state. Registered
 * under id `"refresh"`. Rebuilds and reopens the gui rather than mutating it in place — the
 * `Gui`/`GuiBuilder` split has no shared "re-populate an already-built Gui" operation to hook
 * into, so this is the closest available equivalent.
 */
@ConfigSerializable
class RefreshMenuClickAction : ClickAction {

    override fun id(): String = "refresh"

    override fun handle(event: GuiClickEvent): Boolean {
        MenuNavigator.refresh(event.player)
        return true
    }
}
