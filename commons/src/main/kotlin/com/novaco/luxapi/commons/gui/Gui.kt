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
}