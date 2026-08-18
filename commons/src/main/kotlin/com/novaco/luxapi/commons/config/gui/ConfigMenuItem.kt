package com.novaco.luxapi.commons.config.gui

import com.novaco.luxapi.commons.chat.placeholder.PlaceholderManager
import com.novaco.luxapi.commons.gui.GuiBuilder
import com.novaco.luxapi.commons.gui.GuiItem
import com.novaco.luxapi.commons.player.LuxPlayer
import org.spongepowered.configurate.objectmapping.ConfigSerializable

/**
 * A declaratively-configured GUI item: appearance, slot positions, per-viewer display rules, and
 * click behavior, all editable from YAML. Requires [ConfigGuiRegistries.init] to have run first
 * if [clickActions] or [displayRules] are used.
 *
 * No `amount` field — [GuiItem] doesn't carry a stack count, so one isn't declared here either
 * rather than being silently dropped at render time. Same reasoning excludes enchantments/item
 * flags/NBT — [GuiItem] has no fields for them either, across every platform implementation.
 */
@ConfigSerializable
class ConfigMenuItem {

    var enabled: Boolean = true
    var type: String = "minecraft:stone"
    var name: String = ""
    var lore: MutableList<String> = mutableListOf()
    var customModelData: Int = 0
    var positions: MutableList<Int> = mutableListOf()

    /** Inclusive `"start-end"` slot ranges, e.g. `"10-17"` — merged with [positions] via [effectivePositions]. */
    var positionRanges: MutableList<String> = mutableListOf()

    var clickActions: MutableList<ClickAction> = mutableListOf()
    var displayRules: MutableList<ItemDisplayRule> = mutableListOf()

    /**
     * Ticks between frame changes for an animated item — 0 (default) means static. Only takes
     * effect if [frames] is non-empty; see [isAnimated].
     */
    var refreshTicks: Long = 0

    /** Additional visual frames cycled through when [isAnimated] — this item's own fields are frame 0. */
    var frames: MutableList<MenuItemFrame> = mutableListOf()

    @Transient
    private var frameIndex: Int = 0

    /**
     * Applies [displayRules] in order for [player], short-circuiting to null the moment a rule
     * hides the item. Returns null immediately if [enabled] is false.
     */
    fun resolve(player: LuxPlayer): ConfigMenuItem? {
        if (!enabled) return null

        var current: ConfigMenuItem = this
        for (rule in displayRules) {
            current = rule.resolve(player, current) ?: return null
        }
        return current
    }

    /** [positions] plus every slot covered by [positionRanges], de-duplicated. Malformed ranges are skipped. */
    fun effectivePositions(): List<Int> {
        return (positions + positionRanges.flatMap { parseRange(it) }).distinct()
    }

    /** True if this item should be placed as an animated slot (see [com.novaco.luxapi.commons.gui.GuiBuilder.setAnimatedItem]). */
    fun isAnimated(): Boolean = refreshTicks > 0 && frames.isNotEmpty()

    /** Resolves for [player] and converts to a [GuiItem], or null if hidden. */
    fun toGuiItem(player: LuxPlayer): GuiItem? {
        val resolved = resolve(player) ?: return null
        return resolved.buildGuiItem(player)
    }

    /**
     * Advances and returns the next animation frame's [GuiItem] for [player], or null if hidden.
     * The frame counter lives on the item [nextFrameGuiItem] is called on (not on whatever
     * [resolve] returns), so it persists correctly across repeated calls from the same
     * [com.novaco.luxapi.commons.gui.GuiBuilder.setAnimatedItem] supplier closure even if display
     * rules swap in a different item's frames from tick to tick.
     */
    fun nextFrameGuiItem(player: LuxPlayer): GuiItem? {
        val resolved = resolve(player) ?: return null
        val candidateFrames = resolved.frames
        if (candidateFrames.isEmpty()) return resolved.buildGuiItem(player)

        val frame = candidateFrames[frameIndex % candidateFrames.size]
        frameIndex++
        return resolved.buildGuiItem(player, frame)
    }

    /** Resolves for [player] and places the result into every position from [effectivePositions] on [builder]. */
    fun placeInto(builder: GuiBuilder, player: LuxPlayer) {
        val slots = effectivePositions()
        if (slots.isEmpty()) return

        if (isAnimated()) {
            slots.forEach { slot -> builder.setAnimatedItem(slot, refreshTicks) { nextFrameGuiItem(player) ?: GuiItem(material = "minecraft:air") } }
            return
        }

        val guiItem = toGuiItem(player) ?: return
        slots.forEach { slot -> builder.setItem(slot, guiItem) }
    }

    private fun buildGuiItem(player: LuxPlayer, frame: MenuItemFrame? = null): GuiItem {
        val guiItem = GuiItem(
            material = frame?.type ?: type,
            displayName = PlaceholderManager.replace(player, frame?.name ?: name),
            lore = PlaceholderManager.replaceLines(player, frame?.lore ?: lore),
            customModelData = frame?.customModelData ?: customModelData
        )
        guiItem.onClick { event ->
            for (action in clickActions) {
                if (!action.handle(event)) break
            }
        }
        return guiItem
    }

    private fun parseRange(range: String): List<Int> {
        val parts = range.split("-", limit = 2)
        if (parts.size != 2) return emptyList()
        val start = parts[0].trim().toIntOrNull() ?: return emptyList()
        val end = parts[1].trim().toIntOrNull() ?: return emptyList()
        if (start > end) return emptyList()
        return (start..end).toList()
    }
}
