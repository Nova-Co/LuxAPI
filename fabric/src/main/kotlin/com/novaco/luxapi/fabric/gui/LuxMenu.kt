package com.novaco.luxapi.fabric.gui

import com.novaco.luxapi.commons.gui.ClickType
import com.novaco.luxapi.commons.gui.GuiClickEvent
import com.novaco.luxapi.fabric.player.FabricLuxPlayer
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.ChestMenu
import net.minecraft.world.inventory.MenuType

/**
 * A custom implementation of ChestMenu designed to intercept and manage
 * inventory click packets safely within the LuxAPI architecture.
 */
class LuxMenu(
    containerId: Int,
    playerInventory: net.minecraft.world.entity.player.Inventory,
    private val gui: FabricGui
) : ChestMenu(
    getMenuType(gui.rows),
    containerId,
    playerInventory,
    gui.container,
    gui.rows
) {

    /**
     * Intercepts the click action before the game processes the item movement.
     * Triggers the defined lambda functions inside the GuiItem and enforces UI protection.
     */
    override fun clicked(
        slotId: Int,
        button: Int,
        clickType: net.minecraft.world.inventory.ClickType,
        player: Player
    ) {
        if (slotId in 0 until (gui.rows * 9)) {
            val guiItem = gui.getItem(slotId)

            if (guiItem != null) {
                val luxPlayer = FabricLuxPlayer(player as net.minecraft.server.level.ServerPlayer)
                val mappedClick = mapClickType(clickType, button)
                val event = GuiClickEvent(luxPlayer, slotId, mappedClick, gui)
                guiItem.clickHandler?.invoke(event)

                if (event.isCancelled) {
                    return
                }
            } else {
                return
            }
        } else if (clickType == net.minecraft.world.inventory.ClickType.QUICK_MOVE) {
            return
        }

        super.clicked(slotId, button, clickType, player)
    }

    companion object {
        /**
         * Resolves the appropriate native MenuType based on the required row count.
         */
        fun getMenuType(rows: Int): MenuType<ChestMenu> {
            return when (rows) {
                1 -> MenuType.GENERIC_9x1
                2 -> MenuType.GENERIC_9x2
                3 -> MenuType.GENERIC_9x3
                4 -> MenuType.GENERIC_9x4
                5 -> MenuType.GENERIC_9x5
                6 -> MenuType.GENERIC_9x6
                else -> MenuType.GENERIC_9x3
            }
        }

        /**
         * Translates native Minecraft click interactions into the universal LuxAPI format.
         */
        fun mapClickType(clickType: net.minecraft.world.inventory.ClickType, button: Int): ClickType {
            return when (clickType) {
                net.minecraft.world.inventory.ClickType.PICKUP -> if (button == 0) ClickType.LEFT else ClickType.RIGHT
                net.minecraft.world.inventory.ClickType.QUICK_MOVE -> if (button == 0) ClickType.SHIFT_LEFT else ClickType.SHIFT_RIGHT
                net.minecraft.world.inventory.ClickType.CLONE -> ClickType.MIDDLE
                net.minecraft.world.inventory.ClickType.THROW -> ClickType.DROP
                else -> ClickType.UNKNOWN
            }
        }
    }
}