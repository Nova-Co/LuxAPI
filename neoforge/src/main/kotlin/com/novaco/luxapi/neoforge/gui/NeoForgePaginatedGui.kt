package com.novaco.luxapi.neoforge.gui

import com.novaco.luxapi.commons.gui.GuiItem
import com.novaco.luxapi.commons.gui.PaginatedGui
import com.novaco.luxapi.commons.player.LuxPlayer

/**
 * NeoForge implementation of a paginated GUI.
 */
class NeoForgePaginatedGui(
    title: String,
    rows: Int,
    staticItems: Map<Int, GuiItem>,
    private val globalItems: List<GuiItem>,
    private val contentSlots: List<Int>
) : NeoForgeGui(title, rows, staticItems), PaginatedGui {

    private val playerPages = mutableMapOf<LuxPlayer, Int>()

    override fun open(player: LuxPlayer) {
        playerPages[player] = 0
        renderPage(player)
        super.open(player)
    }

    override fun setPage(player: LuxPlayer, page: Int) {
        val targetPage = page.coerceIn(0, getTotalPages() - 1)
        playerPages[player] = targetPage
        renderPage(player)
        refresh(player)
    }

    override fun getCurrentPage(player: LuxPlayer): Int {
        return playerPages[player] ?: 0
    }

    override fun getTotalPages(): Int {
        if (contentSlots.isEmpty()) return 1
        return kotlin.math.ceil(globalItems.size.toDouble() / contentSlots.size).toInt().coerceAtLeast(1)
    }

    private fun renderPage(player: LuxPlayer) {
        val page = playerPages[player] ?: 0
        val itemsPerPage = contentSlots.size
        val startIndex = page * itemsPerPage

        contentSlots.forEach { slot ->
            container.setItem(slot, net.minecraft.world.item.ItemStack.EMPTY)
        }

        for (i in 0 until itemsPerPage) {
            val itemIndex = startIndex + i
            if (itemIndex < globalItems.size) {
                val slot = contentSlots[i]
                container.setItem(slot, buildItemStack(globalItems[itemIndex]))
            }
        }
    }
}