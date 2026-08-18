package com.novaco.luxapi.commons.config.gui.action

import com.novaco.luxapi.commons.config.gui.ClickAction
import com.novaco.luxapi.commons.config.gui.ConfigGuiRegistries
import com.novaco.luxapi.commons.config.type.ConfigItemStack
import com.novaco.luxapi.commons.gui.GuiClickEvent
import org.spongepowered.configurate.objectmapping.ConfigSerializable

/**
 * Hands the clicking player a physical copy of [item]. Registered under id `"give_item"`.
 *
 * Commons has no cross-platform "add an item stack to this player's inventory" hook of its own —
 * only platform code can actually do that. A platform bootstrap must set
 * [ConfigGuiRegistries.itemGiver] for this action to do anything; until then it's a documented
 * no-op rather than a crash.
 */
@ConfigSerializable
class GiveItemClickAction : ClickAction {

    var item: ConfigItemStack = ConfigItemStack()

    override fun id(): String = "give_item"

    override fun handle(event: GuiClickEvent): Boolean {
        ConfigGuiRegistries.itemGiver?.give(event.player, item)
        return true
    }
}
