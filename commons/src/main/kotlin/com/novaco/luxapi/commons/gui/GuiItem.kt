package com.novaco.luxapi.commons.gui

/**
 * Represents a functional item displayed within a graphical user interface.
 *
 * [stack] is an escape hatch for a caller that already has a fully-built platform-native item
 * stack (e.g. a Cobblemon `PokemonItem` carrying species/model data) and wants it placed exactly
 * as-is, on top of which [displayName]/[lore] still apply. Typed [Any] because `commons` has no
 * Minecraft/Bukkit dependency and platforms don't share a single `ItemStack` type; each platform's
 * [Gui] implementation casts it to its own native type and falls back to [material] if it doesn't
 * match.
 */
class GuiItem private constructor(
    val material: String?,
    val stack: Any?,
    val displayName: String = "",
    val lore: List<String> = emptyList(),
    val customModelData: Int = 0
) {
    constructor(
        material: String,
        displayName: String = "",
        lore: List<String> = emptyList(),
        customModelData: Int = 0
    ) : this(material, null, displayName, lore, customModelData)

    constructor(
        stack: Any,
        displayName: String = "",
        lore: List<String> = emptyList()
    ) : this(null, stack, displayName, lore, 0)

    var clickHandler: ((GuiClickEvent) -> Unit)? = null

    /**
     * Assigns a click execution block to this specific GUI item.
     */
    fun onClick(handler: (GuiClickEvent) -> Unit): GuiItem {
        this.clickHandler = handler
        return this
    }
}