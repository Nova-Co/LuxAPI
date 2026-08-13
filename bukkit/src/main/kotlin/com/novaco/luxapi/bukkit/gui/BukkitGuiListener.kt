package com.novaco.luxapi.bukkit.gui

import com.novaco.luxapi.bukkit.player.BukkitLuxPlayer
import com.novaco.luxapi.commons.gui.ClickType
import com.novaco.luxapi.commons.gui.GuiClickEvent
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.plugin.Plugin

/**
 * Single global listener that intercepts clicks inside any [BukkitGui]-backed inventory,
 * identified via [LuxGuiHolder], and routes them into the item's registered click handler —
 * the Bukkit equivalent of `LuxMenu`'s packet interception on Fabric/NeoForge.
 *
 * @param plugin The owning plugin instance, required to register this listener with Bukkit.
 */
class BukkitGuiListener(private val plugin: Plugin) : Listener {

    fun register() {
        plugin.server.pluginManager.registerEvents(this, plugin)
    }

    @EventHandler
    fun onClick(event: InventoryClickEvent) {
        val holder = event.inventory.holder as? LuxGuiHolder ?: return
        val gui = holder.gui
        val clickedSlot = event.rawSlot

        // Only intercept clicks inside the GUI's own top inventory; leave the
        // player's own inventory (bottom half of the open view) untouched.
        if (clickedSlot < 0 || clickedSlot >= gui.rows * 9) return

        val guiItem = gui.getItem(clickedSlot) ?: run {
            event.isCancelled = true
            return
        }

        val bukkitPlayer = event.whoClicked as? Player ?: return
        val luxPlayer = BukkitLuxPlayer(bukkitPlayer)
        val mappedClick = mapClickType(event.click)
        val clickEvent = GuiClickEvent(luxPlayer, clickedSlot, mappedClick, gui)

        guiItem.clickHandler?.invoke(clickEvent)

        event.isCancelled = clickEvent.isCancelled
    }

    companion object {
        /**
         * Translates native Bukkit click interactions into the universal LuxAPI format.
         */
        fun mapClickType(clickType: org.bukkit.event.inventory.ClickType): ClickType {
            return when (clickType) {
                org.bukkit.event.inventory.ClickType.LEFT -> ClickType.LEFT
                org.bukkit.event.inventory.ClickType.RIGHT -> ClickType.RIGHT
                org.bukkit.event.inventory.ClickType.SHIFT_LEFT -> ClickType.SHIFT_LEFT
                org.bukkit.event.inventory.ClickType.SHIFT_RIGHT -> ClickType.SHIFT_RIGHT
                org.bukkit.event.inventory.ClickType.MIDDLE -> ClickType.MIDDLE
                org.bukkit.event.inventory.ClickType.DROP, org.bukkit.event.inventory.ClickType.CONTROL_DROP -> ClickType.DROP
                else -> ClickType.UNKNOWN
            }
        }
    }
}
