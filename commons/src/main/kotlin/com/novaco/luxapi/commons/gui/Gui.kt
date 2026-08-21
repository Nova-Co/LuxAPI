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

    /**
     * Increments whenever this instance's content is overwritten by a *different* logical
     * screen reusing the same underlying container (see the inventory-reuse optimization in
     * each platform's `open`). [hasViewers] alone can't detect this, since the player is still
     * viewing a menu — just not the one this instance originally represented. Long-running tasks
     * bound to a specific instance (e.g. [GuiBuilder]'s animated slots) capture this value at
     * start and compare it on every tick, self-cancelling if it no longer matches.
     */
    val generation: Int
}