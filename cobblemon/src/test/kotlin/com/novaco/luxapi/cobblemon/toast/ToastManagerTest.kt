package com.novaco.luxapi.cobblemon.toast

import net.minecraft.SharedConstants
import net.minecraft.network.chat.Component
import net.minecraft.server.Bootstrap
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test

class ToastManagerTest {

    companion object {
        @JvmStatic
        @BeforeAll
        fun bootstrapMinecraft() {
            SharedConstants.tryDetectVersion()
            Bootstrap.bootStrap()
        }
    }

    // No ServerPlayer listeners are passed in these tests: Toast.addListeners iterates
    // an empty vararg array and returns immediately, so this never touches
    // Cobblemon.implementation's network manager (which isn't initialized outside a
    // running server) — only ToastManager's own wiring logic is under test here.

    @Test
    fun `sendToast builds a Toast with the given title, description and icon`() {
        val title = Component.literal("Lux Test Toast")
        val description = Component.literal("Lux Test Description")
        val icon = ItemStack(Items.DIAMOND)

        val toast = ToastManager.sendToast(emptyList(), title, description, icon)

        assertEquals(title, toast.title)
        assertEquals(description, toast.description)
        assertEquals(icon, toast.icon)
    }

    @Test
    fun `sendToast defaults icon to empty and leaves progress unset`() {
        val toast = ToastManager.sendToast(emptyList(), Component.literal("T"), Component.literal("D"))

        assertEquals(ItemStack.EMPTY, toast.icon)
        assertEquals(-1F, toast.progress)
    }
}
