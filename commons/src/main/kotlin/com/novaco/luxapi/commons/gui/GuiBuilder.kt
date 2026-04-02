package com.novaco.luxapi.commons.gui

/**
 * A builder pattern for constructing instances of LuxGui efficiently.
 */
abstract class GuiBuilder {

    protected var title: String = "Menu"
    protected var rows: Int = 3
    protected val items = mutableMapOf<Int, GuiItem>()

    /**
     * Sets the display title of the graphical user interface.
     */
    fun title(title: String): GuiBuilder {
        this.title = title
        return this
    }

    /**
     * Sets the number of rows for the graphical user interface.
     */
    fun rows(rows: Int): GuiBuilder {
        require(rows in 1..6) { "Rows must be between 1 and 6" }
        this.rows = rows
        return this
    }

    /**
     * Places a specific GUI item into the specified slot.
     */
    fun setItem(slot: Int, item: GuiItem): GuiBuilder {
        require(slot in 0 until (rows * 9)) { "Slot $slot is out of bounds for $rows rows." }
        this.items[slot] = item
        return this
    }

    /**
     * Finalizes the configuration and builds the platform-specific graphical user interface.
     */
    abstract fun build(): Gui
}