package com.novaco.luxapi.commons.gui

/**
 * A specialized builder for creating multi-page graphical user interfaces.
 * Automatically distributes a list of items across multiple pages.
 */
abstract class PaginatedGuiBuilder : GuiBuilder() {

    protected val globalItemList = mutableListOf<GuiItem>()
    protected val contentSlotList = mutableListOf<Int>()
    protected var nextButtonSlot: Int = -1
    protected var previousButtonSlot: Int = -1

    override fun title(title: String): PaginatedGuiBuilder {
        super.title(title)
        return this
    }

    override fun rows(rows: Int): PaginatedGuiBuilder {
        super.rows(rows)
        return this
    }

    override fun setItem(slot: Int, item: GuiItem): PaginatedGuiBuilder {
        super.setItem(slot, item)
        return this
    }

    override fun fillBorder(item: GuiItem): PaginatedGuiBuilder {
        super.fillBorder(item)
        return this
    }

    override fun fillEmpty(item: GuiItem): PaginatedGuiBuilder {
        super.fillEmpty(item)
        return this
    }
    // -----------------------------------------------------------------

    /**
     * Adds a list of items to be distributed across pages.
     */
    fun globalItems(items: List<GuiItem>): PaginatedGuiBuilder {
        this.globalItemList.addAll(items)
        return this
    }

    /**
     * Defines the specific slots where the global items should be displayed.
     */
    fun contentSlots(slots: List<Int>): PaginatedGuiBuilder {
        this.contentSlotList.addAll(slots)
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