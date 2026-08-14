package com.novaco.luxapi.commons.gui

import com.novaco.luxapi.commons.player.LuxPlayer

/**
 * Represents a cross-platform graphical user interface container.
 */
interface Gui {

    /**
     * Opens this graphical user interface for the specified player.
     */
    fun open(player: LuxPlayer)

    /**
     * Closes this graphical user interface for the specified player.
     */
    fun close(player: LuxPlayer)

    /**
     * Updates or sets a specific item in the graphical user interface.
     */
    fun setItem(slot: Int, item: GuiItem)

    /**
     * Retrieves the item currently set at the specified slot.
     */
    fun getItem(slot: Int): GuiItem?

    /**
     * Refreshes the items currently displayed in the GUI without closing the container.
     * Useful for real-time data updates or animations.
     */
    fun refresh(player: LuxPlayer)

    /**
     * Refreshes the items currently displayed for every player currently viewing this GUI,
     * without the caller needing to know who they are. Used by [GuiBuilder]'s animated slots.
     */
    fun refreshAll()

    /**
     * True if at least one player currently has this GUI open. Used by animated slots to stop
     * their repeating scheduler task once nobody's watching anymore.
     */
    fun hasViewers(): Boolean
}