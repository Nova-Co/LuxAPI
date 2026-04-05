package com.novaco.luxapi.fabric.gui

import com.novaco.luxapi.commons.gui.ClickType as LuxClickType
import net.minecraft.world.inventory.ClickType as NativeClickType
import net.minecraft.world.inventory.MenuType
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class LuxMenuTest {

    @Test
    fun `test menu type row mapping`() {
        // Testing that the row sizes correctly fetch the corresponding generic Chest menu
        assertEquals(MenuType.GENERIC_9x1, LuxMenu.getMenuType(1))
        assertEquals(MenuType.GENERIC_9x3, LuxMenu.getMenuType(3))
        assertEquals(MenuType.GENERIC_9x6, LuxMenu.getMenuType(6))

        // Testing fallback
        assertEquals(MenuType.GENERIC_9x3, LuxMenu.getMenuType(99), "Invalid row counts should default to 3 rows.")
    }

    @Test
    fun `test click type mapping`() {
        // Native PICKUP (Standard clicks)
        assertEquals(LuxClickType.LEFT, LuxMenu.mapClickType(NativeClickType.PICKUP, 0))
        assertEquals(LuxClickType.RIGHT, LuxMenu.mapClickType(NativeClickType.PICKUP, 1))

        // Native QUICK_MOVE (Shift clicks)
        assertEquals(LuxClickType.SHIFT_LEFT, LuxMenu.mapClickType(NativeClickType.QUICK_MOVE, 0))
        assertEquals(LuxClickType.SHIFT_RIGHT, LuxMenu.mapClickType(NativeClickType.QUICK_MOVE, 1))

        // Native CLONE (Middle click)
        assertEquals(LuxClickType.MIDDLE, LuxMenu.mapClickType(NativeClickType.CLONE, 0))

        // Native THROW (Drop key)
        assertEquals(LuxClickType.DROP, LuxMenu.mapClickType(NativeClickType.THROW, 0))
    }
}