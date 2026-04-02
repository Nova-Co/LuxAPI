package com.novaco.luxapi.commons.gui

import com.novaco.luxapi.commons.player.LuxPlayer

/**
 * A specialized builder for creating multi-page graphical user interfaces.
 * Automatically distributes a list of items across multiple pages.
 */
abstract class PaginatedGuiBuilder : GuiBuilder() {

    protected val globalItems = mutableListOf<GuiItem>()
    protected val contentSlots = mutableListOf<Int>()
    protected var nextButtonSlot: Int = -1
    protected var previousButtonSlot: Int = -1

    /**
     * Adds a list of items to be distributed across pages.
     */
    fun items(items: List<GuiItem>): PaginatedGuiBuilder {
        this.globalItems.addAll(items)
        return this
    }

    /**
     * Defines the specific slots where the global items should be displayed.
     */
    fun contentSlots(slots: List<Int>): PaginatedGuiBuilder {
        this.contentSlots.addAll(slots)
        return this
    }

    /**
     * Sets the slot and item for the next page navigation.
     */
    fun nextButton(slot: Int, item: GuiItem): PaginatedGuiBuilder {
        this.nextButtonSlot = slot
        this.setItem(slot, item)
        return this
    }

    /**
     * Sets the slot and item for the previous page navigation.
     */
    fun previousButton(slot: Int, item: GuiItem): PaginatedGuiBuilder {
        this.previousButtonSlot = slot
        this.setItem(slot, item)
        return this
    }

    /**
     * Builds the platform-specific paginated interface.
     */
    abstract override fun build(): PaginatedGui
}