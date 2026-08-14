package com.novaco.luxapi.fabric.gui

import com.novaco.luxapi.commons.gui.Gui
import com.novaco.luxapi.commons.gui.GuiBuilder

/**
 * The Fabric implementation of the LuxGuiBuilder.
 * Translates builder parameters into a native Fabric GUI container.
 */
class FabricGuiBuilder : GuiBuilder() {

    /**
     * Constructs and returns the fully built FabricLuxGui instance.
     */
    override fun build(): Gui {
        val gui = FabricGui(title, rows, items)
        startAnimations(gui)
        return gui
    }
}