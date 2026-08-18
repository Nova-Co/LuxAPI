package com.novaco.luxapi.commons.config.gui

import com.novaco.luxapi.commons.config.serializer.PolymorphicConfigEntry
import com.novaco.luxapi.commons.gui.GuiClickEvent
import org.spongepowered.configurate.objectmapping.ConfigSerializable

/**
 * A declaratively-configured response to a [ConfigMenuItem] click. Implementations are resolved
 * by discriminator id via [ConfigGuiRegistries] — register custom ones with
 * `ConfigGuiRegistries.clickActions.register(id, YourAction::class.java)` before loading any
 * config that references them.
 */
@ConfigSerializable
interface ClickAction : PolymorphicConfigEntry {

    /**
     * Runs this action in response to [event]. [GuiClickEvent] already carries the gui, player,
     * slot, and click type, so no separate context type is needed here.
     *
     * @return false to stop any further [ClickAction]s in the same item's list from running —
     * e.g. [action.CooldownClickAction] gating whatever comes after it. true (the common case)
     * lets the rest of the list run.
     */
    fun handle(event: GuiClickEvent): Boolean
}
