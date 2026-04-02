package com.novaco.luxapi.fabric.gui

import com.novaco.luxapi.commons.gui.PaginatedGui
import com.novaco.luxapi.commons.gui.PaginatedGuiBuilder

/**
 * The Fabric implementation of the PaginatedGuiBuilder.
 * Translates builder parameters into a native Fabric paginated GUI container.
 */
class FabricPaginatedGuiBuilder : PaginatedGuiBuilder() {

    /**
     * Constructs and returns the fully built FabricPaginatedGui instance.
     */
    override fun build(): PaginatedGui {
        return FabricPaginatedGui(title, rows, items, globalItemList, contentSlotList)
    }
}