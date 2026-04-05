package com.novaco.luxapi.neoforge.gui

import com.novaco.luxapi.commons.gui.ClickType as LuxClickType
import net.minecraft.world.inventory.ClickType as NativeClickType
import net.minecraft.world.inventory.MenuType
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class NeoForgeMenuTest {

    @Test
    fun `test menu type row mapping`() {
        // Testing that the row sizes correctly fetch the corresponding generic Chest menu
        assertEquals(MenuType.GENERIC_9x1, NeoForgeMenu.getMenuType(1))
        assertEquals(MenuType.GENERIC_9x3, NeoForgeMenu.getMenuType(3))
        assertEquals(MenuType.GENERIC_9x6, NeoForgeMenu.getMenuType(6))

        // Testing fallback
        assertEquals(MenuType.GENERIC_9x3, NeoForgeMenu.getMenuType(99), "Invalid row counts should default to 3 rows.")
    }

    @Test
    fun `test click type mapping`() {
        // Native PICKUP (Standard clicks)
        assertEquals(LuxClickType.LEFT, NeoForgeMenu.mapClickType(NativeClickType.PICKUP, 0))
        assertEquals(LuxClickType.RIGHT, NeoForgeMenu.mapClickType(NativeClickType.PICKUP, 1))

        // Native QUICK_MOVE (Shift clicks)
        assertEquals(LuxClickType.SHIFT_LEFT, NeoForgeMenu.mapClickType(NativeClickType.QUICK_MOVE, 0))
        assertEquals(LuxClickType.SHIFT_RIGHT, NeoForgeMenu.mapClickType(NativeClickType.QUICK_MOVE, 1))

        // Native CLONE (Middle click)
        assertEquals(LuxClickType.MIDDLE, NeoForgeMenu.mapClickType(NativeClickType.CLONE, 0))

        // Native THROW (Drop key)
        assertEquals(LuxClickType.DROP, NeoForgeMenu.mapClickType(NativeClickType.THROW, 0))
    }
}