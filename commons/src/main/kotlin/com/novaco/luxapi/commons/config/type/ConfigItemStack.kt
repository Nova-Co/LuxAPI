package com.novaco.luxapi.commons.config.type

import org.spongepowered.configurate.objectmapping.ConfigSerializable

/**
 * A physical item description — material, stack size, optional name/lore — distinct from
 * [com.novaco.luxapi.commons.config.gui.ConfigMenuItem], which is display-only and has no
 * `amount` since [com.novaco.luxapi.commons.gui.GuiItem] doesn't carry one. Used by
 * [com.novaco.luxapi.commons.config.gui.action.GiveItemClickAction].
 */
@ConfigSerializable
class ConfigItemStack {
    var type: String = "minecraft:stone"
    var amount: Int = 1
    var name: String = ""
    var lore: MutableList<String> = mutableListOf()
}
