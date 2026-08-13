package com.novaco.luxapi.cobblemon.cooking

import com.cobblemon.mod.common.api.cooking.Flavour
import net.minecraft.SharedConstants
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.Bootstrap
import net.minecraft.world.item.DyeColor
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test

class CookingManagerTest {

    companion object {
        @JvmStatic
        @BeforeAll
        fun bootstrapMinecraft() {
            SharedConstants.tryDetectVersion()
            Bootstrap.bootStrap()
        }
    }

    @Test
    fun `getBerry returns null in the unpopulated JSON-loaded registry`() {
        assertNull(CookingManager.getBerry("not_a_real_lux_berry"))
    }

    @Test
    fun `getFlavours returns null for a stack with no seasoning or flavour component`() {
        assertNull(CookingManager.getFlavours(ItemStack(Items.DIAMOND)))
    }

    @Test
    fun `registerSeasoning makes a matching stack resolvable via getFlavours`() {
        val flavours = mapOf(Flavour.SWEET to 10, Flavour.SOUR to 5)

        CookingManager.registerSeasoning(ResourceLocation.withDefaultNamespace("apple"), flavours, DyeColor.RED)
        val result = CookingManager.getFlavours(ItemStack(Items.APPLE))

        assertEquals(flavours, result)
    }
}
