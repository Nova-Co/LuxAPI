package com.novaco.luxapi.fabric.gui

import com.novaco.luxapi.commons.gui.GuiItem
import com.novaco.luxapi.fabric.player.FabricLuxPlayer
import net.minecraft.SharedConstants
import net.minecraft.server.Bootstrap
import net.minecraft.server.level.ServerPlayer
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*

class FabricPaginatedGuiTest {

    companion object {
        @JvmStatic
        @BeforeAll
        fun initMinecraft() {
            SharedConstants.tryDetectVersion()
            Bootstrap.bootStrap()
        }
    }

    private fun createMockPlayer(): FabricLuxPlayer {
        val mockServerPlayer = mock<ServerPlayer>()
        val mockLuxPlayer = mock<FabricLuxPlayer>()
        whenever(mockLuxPlayer.parent).thenReturn(mockServerPlayer)

        // Setup dummy container menu for refresh logic
        val mockContainerMenu = mock<net.minecraft.world.inventory.AbstractContainerMenu>()
        val menuField = ServerPlayer::class.java.getField("containerMenu")
        menuField.isAccessible = true
        menuField.set(mockServerPlayer, mockContainerMenu)

        return mockLuxPlayer
    }

    @Test
    fun `test pagination math and page boundaries`() {
        // Create 10 global items
        val globalItems = List(10) { GuiItem("minecraft:stone") }
        // 4 slots available per page
        val contentSlots = listOf(10, 11, 12, 13)

        val gui = FabricPaginatedGuiBuilder()
            .title("Paginated")
            .rows(3)
            .globalItems(globalItems)
            .contentSlots(contentSlots)
            .build() as FabricPaginatedGui

        val player = createMockPlayer()
        gui.open(player)

        // 10 items / 4 slots = 2.5 -> Ceiled to 3 total pages
        assertEquals(3, gui.getTotalPages(), "Should calculate exactly 3 pages.")

        // Test Boundary Coercion
        gui.setPage(player, 5) // Try to go past the max page
        assertEquals(2, gui.getCurrentPage(player), "Should clamp to max page index (2).")

        gui.setPage(player, -5) // Try to go below 0
        assertEquals(0, gui.getCurrentPage(player), "Should clamp to minimum page index (0).")
    }

    @Test
    fun `test slot rendering updates container accurately`() {
        val globalItems = listOf(
            GuiItem("minecraft:apple"),
            GuiItem("minecraft:stick"),
            GuiItem("minecraft:diamond")
        )
        // 2 slots per page
        val contentSlots = listOf(0, 1)

        val gui = FabricPaginatedGui("Test", 1, emptyMap(), globalItems, contentSlots)
        val player = createMockPlayer()

        // Page 0 (Items 0 and 1)
        gui.open(player)
        assertEquals("minecraft:apple", gui.container.getItem(0).item.toString())
        assertEquals("minecraft:stick", gui.container.getItem(1).item.toString())

        // Page 1 (Item 2)
        gui.setPage(player, 1)
        assertEquals("minecraft:diamond", gui.container.getItem(0).item.toString())
        assertTrue(gui.container.getItem(1).isEmpty, "Slot 1 should be EMPTY because there are no more items.")
    }
}