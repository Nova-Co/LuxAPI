package com.novaco.luxapi.bukkit.gui

import com.novaco.luxapi.commons.gui.PaginatedGui
import com.novaco.luxapi.commons.gui.PaginatedGuiBuilder

/**
 * The Bukkit implementation of [PaginatedGuiBuilder].
 */
class BukkitPaginatedGuiBuilder : PaginatedGuiBuilder() {

    override fun build(): PaginatedGui {
        val gui = BukkitPaginatedGui(title, rows, items, globalItemList, contentSlotList)
        startAnimations(gui)
        return gui
    }
}
