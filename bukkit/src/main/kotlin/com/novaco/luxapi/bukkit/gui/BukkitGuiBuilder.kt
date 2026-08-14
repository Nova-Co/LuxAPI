package com.novaco.luxapi.bukkit.gui

import com.novaco.luxapi.commons.gui.Gui
import com.novaco.luxapi.commons.gui.GuiBuilder

/**
 * The Bukkit implementation of [GuiBuilder].
 */
class BukkitGuiBuilder : GuiBuilder() {

    override fun build(): Gui {
        val gui = BukkitGui(title, rows, items)
        startAnimations(gui)
        return gui
    }
}
