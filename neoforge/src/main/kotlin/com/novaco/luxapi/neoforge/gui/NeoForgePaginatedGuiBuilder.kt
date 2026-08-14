package com.novaco.luxapi.neoforge.gui

import com.novaco.luxapi.commons.gui.PaginatedGui
import com.novaco.luxapi.commons.gui.PaginatedGuiBuilder

/**
 * The NeoForge implementation of the PaginatedGuiBuilder.
 */
class NeoForgePaginatedGuiBuilder : PaginatedGuiBuilder() {

    override fun build(): PaginatedGui {
        val gui = NeoForgePaginatedGui(title, rows, items, globalItemList, contentSlotList)
        startAnimations(gui)
        return gui
    }
}