package com.novaco.luxapi.commons.config.gui.action

import com.novaco.luxapi.commons.config.gui.ClickAction
import com.novaco.luxapi.commons.config.gui.ConfigGuiRegistries
import com.novaco.luxapi.commons.gui.GuiClickEvent
import org.spongepowered.configurate.objectmapping.ConfigSerializable

/**
 * Plays a sound for the clicking player. Registered under id `"sound"`.
 *
 * Same pattern as [GiveItemClickAction]/[ExecuteCommandsClickAction] — needs
 * [ConfigGuiRegistries.soundPlayer] set by platform bootstrap; a documented no-op until then.
 */
@ConfigSerializable
class SoundClickAction : ClickAction {

    var sound: String = "minecraft:ui.button.click"
    var volume: Float = 1f
    var pitch: Float = 1f

    override fun id(): String = "sound"

    override fun handle(event: GuiClickEvent): Boolean {
        ConfigGuiRegistries.soundPlayer?.play(event.player, sound, volume, pitch)
        return true
    }
}
