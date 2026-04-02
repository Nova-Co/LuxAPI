package com.novaco.luxapi.commons.gui

/**
 * Represents a functional item displayed within a graphical user interface.
 */
class GuiItem(
    val material: String,
    val displayName: String = "",
    val lore: List<String> = emptyList(),
    val customModelData: Int = 0
) {
    var clickHandler: ((GuiClickEvent) -> Unit)? = null

    /**
     * Assigns a click execution block to this specific GUI item.
     */
    fun onClick(handler: (GuiClickEvent) -> Unit): GuiItem {
        this.clickHandler = handler
        return this
    }
}