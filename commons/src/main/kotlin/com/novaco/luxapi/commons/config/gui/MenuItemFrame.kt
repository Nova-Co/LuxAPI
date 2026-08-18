package com.novaco.luxapi.commons.config.gui

import org.spongepowered.configurate.objectmapping.ConfigSerializable

/** One visual frame of an animated [ConfigMenuItem] — see [ConfigMenuItem.frames]. */
@ConfigSerializable
class MenuItemFrame {
    var type: String = "minecraft:stone"
    var name: String = ""
    var lore: MutableList<String> = mutableListOf()
    var customModelData: Int = 0
}
