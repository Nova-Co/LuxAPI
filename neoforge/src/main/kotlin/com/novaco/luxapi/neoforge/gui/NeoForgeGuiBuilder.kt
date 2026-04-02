package com.novaco.luxapi.neoforge.gui

import com.novaco.luxapi.commons.gui.Gui
import com.novaco.luxapi.commons.gui.GuiBuilder

/**
 * The NeoForge implementation of the GuiBuilder.
 */
class NeoForgeGuiBuilder : GuiBuilder() {

    override fun build(): Gui {
        return NeoForgeGui(title, rows, items)
    }
}