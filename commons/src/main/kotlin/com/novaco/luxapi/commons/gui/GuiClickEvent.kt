package com.novaco.luxapi.commons.gui

import com.novaco.luxapi.commons.player.LuxPlayer

/**
 * Represents the event triggered when a player clicks an item inside a LuxGui.
 */
data class GuiClickEvent(
    val player: LuxPlayer,
    val slot: Int,
    val clickType: ClickType,
    val gui: Gui
) {
    /**
     * Determines whether the default item-moving behavior should be cancelled.
     * Defaults to true to protect the GUI layout.
     */
    var isCancelled: Boolean = true
}