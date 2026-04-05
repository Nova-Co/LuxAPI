package com.novaco.luxapi.neoforge.gui

import com.novaco.luxapi.commons.gui.GuiItem
import com.novaco.luxapi.neoforge.player.NeoForgeLuxPlayer
import net.minecraft.SharedConstants
import net.minecraft.core.component.DataComponents
import net.minecraft.server.Bootstrap
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.MenuProvider
import net.minecraft.world.inventory.AbstractContainerMenu
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*

class NeoForgeGuiTest {

    companion object {
        @JvmStatic
        @BeforeAll
        fun initMinecraft() {
            SharedConstants.tryDetectVersion()
            Bootstrap.bootStrap()
        }
    }

    @Test
    fun `test neoforge gui builder creates correct instance`() {
        val gui = NeoForgeGuiBuilder()
            .title("Forge Shop")
            .rows(3)
            .setItem(0, GuiItem("minecraft:apple"))
            .build()

        assertTrue(gui is NeoForgeGui, "Builder must return a NeoForgeGui instance.")
        assertEquals("Forge Shop", (gui as NeoForgeGui).title)
        assertEquals(3, gui.rows)
        assertNotNull(gui.getItem(0), "Item should be transferred to the GUI.")
    }

    @Test
    fun `test gui item maps to native item stack`() {
        // Instantiate using named arguments due to 'val' immutability
        val guiItem = GuiItem(
            material = "minecraft:diamond_sword",
            displayName = "Forge Blade",
            lore = listOf("Epic", "Weapon")
        )

        val gui = NeoForgeGui("Test", 1, mapOf(4 to guiItem))

        // Retrieve the native ItemStack from the internal SimpleContainer
        val nativeStack = gui.container.getItem(4)

        assertFalse(nativeStack.isEmpty, "The slot should not be empty.")

        // Verify Data Components (Name and Lore)
        val nameComponent = nativeStack.get(DataComponents.CUSTOM_NAME)
        assertEquals("Forge Blade", nameComponent?.string, "Custom name should map correctly.")

        val loreComponent = nativeStack.get(DataComponents.LORE)
        val lines = loreComponent?.lines() ?: emptyList()
        assertEquals(2, lines.size, "Lore should have 2 lines.")
        assertEquals("Epic", lines[0].string)
        assertEquals("Weapon", lines[1].string)
    }

    @Test
    fun `test gui open triggers native menu provider`() {
        val mockServerPlayer = mock<ServerPlayer>()
        val mockLuxPlayer = mock<NeoForgeLuxPlayer>()
        whenever(mockLuxPlayer.parent).thenReturn(mockServerPlayer)

        val gui = NeoForgeGui("Test", 1, emptyMap())
        gui.open(mockLuxPlayer)

        // Verify that native API was signaled to open the menu
        verify(mockServerPlayer).openMenu(any<MenuProvider>())
    }

    @Test
    fun `test gui refresh synchronizes remote data`() {
        val mockServerPlayer = mock<ServerPlayer>()
        val mockLuxPlayer = mock<NeoForgeLuxPlayer>()
        val mockContainerMenu = mock<AbstractContainerMenu>()

        whenever(mockLuxPlayer.parent).thenReturn(mockServerPlayer)

        // Use Reflection to safely set the public containerMenu field
        val menuField = ServerPlayer::class.java.getField("containerMenu")
        menuField.isAccessible = true
        menuField.set(mockServerPlayer, mockContainerMenu)

        val gui = NeoForgeGui("Refresh Test", 1, emptyMap())
        gui.refresh(mockLuxPlayer)

        // Verify native packet sync is triggered
        verify(mockContainerMenu).sendAllDataToRemote()
    }
}