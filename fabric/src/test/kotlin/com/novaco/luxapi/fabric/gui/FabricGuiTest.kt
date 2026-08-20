package com.novaco.luxapi.fabric.gui

import com.novaco.luxapi.commons.gui.GuiItem
import com.novaco.luxapi.fabric.player.FabricLuxPlayer
import net.minecraft.SharedConstants
import net.minecraft.core.component.DataComponents
import net.minecraft.server.Bootstrap
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.MenuProvider
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*

class FabricGuiTest {

    companion object {
        @JvmStatic
        @BeforeAll
        fun initMinecraft() {
            SharedConstants.tryDetectVersion()
            Bootstrap.bootStrap()
        }
    }

    @Test
    fun `test fabric gui builder creates correct instance`() {
        val gui = FabricGuiBuilder()
            .title("Shop")
            .rows(3)
            .setItem(0, GuiItem("minecraft:apple"))
            .build()

        assertTrue(gui is FabricGui, "Builder must return a FabricGui instance.")
        assertEquals("Shop", (gui as FabricGui).title)
        assertEquals(3, gui.rows)
        assertNotNull(gui.getItem(0), "Item should be transferred to the GUI.")
    }

    @Test
    fun `test gui item maps to native item stack`() {
        val guiItem = GuiItem(
            material = "minecraft:diamond_sword",
            displayName = "Excalibur",
            lore = listOf("Legendary", "Weapon")
        )

        val gui = FabricGui("Test", 1, mapOf(4 to guiItem))

        // Retrieve the native ItemStack from the internal SimpleContainer
        val nativeStack = gui.container.getItem(4)

        assertFalse(nativeStack.isEmpty, "The slot should not be empty.")

        // Verify Data Components (Name and Lore)
        val nameComponent = nativeStack.get(DataComponents.CUSTOM_NAME)
        assertEquals("Excalibur", nameComponent?.string, "Custom name should map correctly.")

        val loreComponent = nativeStack.get(DataComponents.LORE)
        val lines = loreComponent?.lines() ?: emptyList()
        assertEquals(2, lines.size, "Lore should have 2 lines.")
        assertEquals("Legendary", lines[0].string)
        assertEquals("Weapon", lines[1].string)
    }

    @Test
    fun `test gui item with native stack is placed directly instead of built from material`() {
        val nativeStack = ItemStack(Items.COBBLED_DEEPSLATE)
        val guiItem = GuiItem(
            stack = nativeStack,
            displayName = "Preserved",
            lore = listOf("Native stack")
        )

        val gui = FabricGui("Test", 1, mapOf(4 to guiItem))
        val placedStack = gui.container.getItem(4)

        assertEquals(Items.COBBLED_DEEPSLATE, placedStack.item, "The native stack's item type should be preserved, not rebuilt from material.")
        assertEquals("Preserved", placedStack.get(DataComponents.CUSTOM_NAME)?.string)
    }

    @Test
    fun `test gui open triggers native menu provider`() {
        val mockServerPlayer = mock<ServerPlayer>()
        val mockLuxPlayer = mock<FabricLuxPlayer>()
        whenever(mockLuxPlayer.parent).thenReturn(mockServerPlayer)

        val gui = FabricGui("Test", 1, emptyMap())
        gui.open(mockLuxPlayer)

        // Verify that Fabric API was signaled to open the menu
        verify(mockServerPlayer).openMenu(any<MenuProvider>())
    }

    @Test
    fun `test gui refresh synchronizes remote data`() {
        val mockServerPlayer = mock<ServerPlayer>()
        val mockLuxPlayer = mock<FabricLuxPlayer>()
        val mockContainerMenu = mock<AbstractContainerMenu>()

        whenever(mockLuxPlayer.parent).thenReturn(mockServerPlayer)

        // Public field mapping for Minecraft 1.21.1
        val menuField = ServerPlayer::class.java.getField("containerMenu")
        menuField.isAccessible = true
        menuField.set(mockServerPlayer, mockContainerMenu)

        val gui = FabricGui("Refresh Test", 1, emptyMap())
        gui.refresh(mockLuxPlayer)

        // Verify native packet sync is triggered
        verify(mockContainerMenu).sendAllDataToRemote()
    }

    /**
     * Sets a mock's public `containerMenu` field (Minecraft 1.21.1) to a fresh mock
     * [AbstractContainerMenu] and returns it, for asserting on packet-sync calls.
     */
    private fun stubContainerMenu(serverPlayer: ServerPlayer): AbstractContainerMenu {
        val mockContainerMenu = mock<AbstractContainerMenu>()
        val menuField = ServerPlayer::class.java.getField("containerMenu")
        menuField.isAccessible = true
        menuField.set(serverPlayer, mockContainerMenu)
        return mockContainerMenu
    }

    @Test
    fun `test hasViewers reflects open and close`() {
        val mockServerPlayer = mock<ServerPlayer>()
        val mockLuxPlayer = mock<FabricLuxPlayer>()
        whenever(mockLuxPlayer.parent).thenReturn(mockServerPlayer)
        stubContainerMenu(mockServerPlayer)

        val gui = FabricGui("Viewer Test", 1, emptyMap())
        assertFalse(gui.hasViewers(), "A freshly built GUI should have no viewers.")

        gui.open(mockLuxPlayer)
        assertTrue(gui.hasViewers(), "Opening should register the player as a viewer.")

        gui.close(mockLuxPlayer)
        assertFalse(gui.hasViewers(), "Closing should untrack the player.")
    }

    @Test
    fun `test onViewerRemoved untracks a client-initiated close without calling close`() {
        val mockServerPlayer = mock<ServerPlayer>()
        val mockLuxPlayer = mock<FabricLuxPlayer>()
        whenever(mockLuxPlayer.parent).thenReturn(mockServerPlayer)
        stubContainerMenu(mockServerPlayer)

        val gui = FabricGui("Viewer Test", 1, emptyMap())
        gui.open(mockLuxPlayer)
        assertTrue(gui.hasViewers())

        // Simulates LuxMenu.removed() firing for an ESC/inventory-swap/disconnect close.
        gui.onViewerRemoved(mockServerPlayer)

        assertFalse(gui.hasViewers(), "A client-initiated close should untrack the viewer too.")
    }

    @Test
    fun `test refreshAll synchronizes every tracked viewer`() {
        val firstServerPlayer = mock<ServerPlayer>()
        val firstLuxPlayer = mock<FabricLuxPlayer>()
        whenever(firstLuxPlayer.parent).thenReturn(firstServerPlayer)
        val firstContainerMenu = stubContainerMenu(firstServerPlayer)

        val secondServerPlayer = mock<ServerPlayer>()
        val secondLuxPlayer = mock<FabricLuxPlayer>()
        whenever(secondLuxPlayer.parent).thenReturn(secondServerPlayer)
        val secondContainerMenu = stubContainerMenu(secondServerPlayer)

        val gui = FabricGui("Refresh All Test", 1, emptyMap())
        gui.open(firstLuxPlayer)
        gui.open(secondLuxPlayer)

        gui.refreshAll()

        verify(firstContainerMenu).sendAllDataToRemote()
        verify(secondContainerMenu).sendAllDataToRemote()
    }
}