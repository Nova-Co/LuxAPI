package com.novaco.luxapi.commons.config.gui

import com.novaco.luxapi.commons.chat.placeholder.PlaceholderManager
import com.novaco.luxapi.commons.gui.GuiBuilder
import com.novaco.luxapi.commons.gui.GuiItem
import com.novaco.luxapi.commons.player.LuxPlayer
import org.spongepowered.configurate.objectmapping.ConfigSerializable

/**
 * A declaratively-configured GUI layout: title, size, static items, and a filler pattern for
 * empty slots. Converts into an existing [GuiBuilder] via [populate] rather than a concrete
 * [com.novaco.luxapi.commons.gui.Gui] itself, since [GuiBuilder.build] is platform-specific —
 * callers supply their platform's builder and get it back populated, ready to `.build()`.
 *
 * Call [populate] fresh per viewer when [items] use permission-gated [ItemDisplayRule]s, since a
 * built [com.novaco.luxapi.commons.gui.Gui]'s items are shared across every viewer, not resolved
 * per-player.
 *
 * If [id] is set, [populate] self-registers this instance into [MenuRegistry] under it — so once
 * any entry point (a command, an event listener) has shown this menu at least once, it becomes a
 * valid [action.OpenMenuClickAction]/[action.BackClickAction] target from anywhere else. This
 * doesn't remove the need for *something* to hold a direct reference to open a menu the very
 * first time — no id-based registry can shortcut that — but it does mean the graph only needs
 * one manual entry point instead of an explicit `MenuRegistry.register()` call per menu.
 */
@ConfigSerializable
open class ConfigGuiInterface {

    enum class FillType { NONE, BLOCK, ALTERNATING, CHECKERED }

    /** Registers this menu into [MenuRegistry] under this id on first [populate] — blank (default) skips it. */
    var id: String = ""

    var title: String = "Menu"
    var rows: Int = 3
    var fillType: FillType = FillType.NONE
    var filler: ConfigMenuItem? = null
    var items: MutableList<ConfigMenuItem> = mutableListOf()

    /** Run when [com.novaco.luxapi.commons.config.gui.MenuNavigator] opens this menu — see its class doc for the caveat on that. */
    var onOpen: MutableList<ClickAction> = mutableListOf()

    open fun populate(builder: GuiBuilder, player: LuxPlayer): GuiBuilder {
        if (id.isNotBlank()) MenuRegistry.register(id, this)

        builder.title(PlaceholderManager.replace(player, title))
        builder.rows(rows)

        val usedSlots = mutableSetOf<Int>()
        items.forEach { item ->
            val slots = item.effectivePositions()
            if (slots.isEmpty()) return@forEach

            if (item.isAnimated()) {
                slots.forEach { slot ->
                    builder.setAnimatedItem(slot, item.refreshTicks) { item.nextFrameGuiItem(player) ?: GuiItem(material = "minecraft:air") }
                    usedSlots += slot
                }
            } else {
                val guiItem = item.toGuiItem(player)
                if (guiItem != null) {
                    slots.forEach { slot ->
                        builder.setItem(slot, guiItem)
                        usedSlots += slot
                    }
                }
            }
        }

        applyFiller(builder, usedSlots, player)
        return builder
    }

    private fun applyFiller(builder: GuiBuilder, usedSlots: Set<Int>, player: LuxPlayer) {
        if (fillType == FillType.NONE) return
        val fillerItem = filler?.toGuiItem(player) ?: return

        when (fillType) {
            FillType.BLOCK -> builder.fillEmpty(fillerItem)
            FillType.ALTERNATING -> fillPattern(builder, usedSlots, fillerItem) { slot -> (slot % 9) % 2 == 0 }
            FillType.CHECKERED -> fillPattern(builder, usedSlots, fillerItem) { slot -> ((slot / 9) + (slot % 9)) % 2 == 0 }
            FillType.NONE -> Unit
        }
    }

    private fun fillPattern(builder: GuiBuilder, usedSlots: Set<Int>, fillerItem: GuiItem, predicate: (Int) -> Boolean) {
        for (slot in 0 until rows * 9) {
            if (slot !in usedSlots && predicate(slot)) {
                builder.setItem(slot, fillerItem)
            }
        }
    }
}
