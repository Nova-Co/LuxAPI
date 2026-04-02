package com.novaco.luxapi.commons.gui

/**
 * A builder pattern for constructing instances of LuxGui efficiently.
 * Supports automated filling and complex layout configurations.
 */
abstract class GuiBuilder {

    protected var title: String = "Menu"
    protected var rows: Int = 3
    protected val items = mutableMapOf<Int, GuiItem>()

    /**
     * Sets the display title of the graphical user interface.
     */
    open fun title(title: String): GuiBuilder {
        this.title = title
        return this
    }

    /**
     * Sets the number of rows for the graphical user interface.
     */
    open fun rows(rows: Int): GuiBuilder {
        require(rows in 1..6) { "Rows must be between 1 and 6" }
        this.rows = rows
        return this
    }

    /**
     * Places a specific GUI item into the specified slot.
     */
    open fun setItem(slot: Int, item: GuiItem): GuiBuilder {
        val maxSlot = rows * 9
        if (slot in 0 until maxSlot) {
            this.items[slot] = item
        }
        return this
    }

    /**
     * Fills the entire border of the GUI with a background item.
     * Calculated based on the current number of rows.
     */
    open fun fillBorder(item: GuiItem): GuiBuilder {
        val totalSlots = rows * 9
        for (i in 0 until totalSlots) {
            val isTopRow = i < 9
            val isBottomRow = i >= totalSlots - 9
            val isLeftColumn = i % 9 == 0
            val isRightColumn = i % 9 == 8

            if (isTopRow || isBottomRow || isLeftColumn || isRightColumn) {
                if (!items.containsKey(i)) {
                    setItem(i, item)
                }
            }
        }
        return this
    }

    /**
     * Fills all empty slots in the current GUI layout with a specific item.
     */
    open fun fillEmpty(item: GuiItem): GuiBuilder {
        for (i in 0 until (rows * 9)) {
            if (!items.containsKey(i)) {
                setItem(i, item)
            }
        }
        return this
    }

    /**
     * Finalizes the configuration and builds the platform-specific graphical user interface.
     */
    abstract fun build(): Gui
}