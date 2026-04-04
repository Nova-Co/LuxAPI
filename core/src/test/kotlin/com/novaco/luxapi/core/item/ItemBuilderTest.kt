package com.novaco.luxapi.core.item

import net.minecraft.SharedConstants
import net.minecraft.core.component.DataComponents
import net.minecraft.server.Bootstrap
import net.minecraft.world.item.Items
import net.minecraft.world.item.component.CustomModelData
import net.minecraft.world.item.component.ItemLore
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test

class ItemBuilderTest {

    companion object {
        @JvmStatic
        @BeforeAll
        fun initMinecraft() {
            // Absolutely required to test ItemStack and DataComponents outside of a running server
            SharedConstants.tryDetectVersion()
            Bootstrap.bootStrap()
        }
    }

    @Test
    fun `test item builder default initialization and amount`() {
        // Create an Apple with a default amount of 1, then change it to 5
        val stack = ItemBuilder(Items.APPLE)
            .amount(5)
            .build()

        assertEquals(Items.APPLE, stack.item, "The item type should match.")
        assertEquals(5, stack.count, "The amount should be updated to 5.")
    }

    @Test
    fun `test custom display name injection`() {
        val stack = ItemBuilder(Items.STICK)
            .name("&6Magic Wand")
            .build()

        val nameComponent = stack.get(DataComponents.CUSTOM_NAME)
        assertNotNull(nameComponent, "The item must have the CUSTOM_NAME component.")
        assertEquals("§6Magic Wand", nameComponent?.string, "The name should be color-formatted and set correctly.")
    }

    @Test
    fun `test lore line additions`() {
        val stack = ItemBuilder(Items.DIAMOND_SWORD)
            .addLore("&7A legendary sword")
            .lore("&cDamage: 100", "&aSpeed: Fast")
            .build()

        val loreComponent: ItemLore? = stack.get(DataComponents.LORE)
        assertNotNull(loreComponent, "The item must have the LORE component.")

        val lines = loreComponent!!.lines()
        assertEquals(3, lines.size, "There should be exactly 3 lines of lore.")
        assertEquals("§7A legendary sword", lines[0].string)
        assertEquals("§cDamage: 100", lines[1].string)
        assertEquals("§aSpeed: Fast", lines[2].string)
    }

    @Test
    fun `test custom model data component`() {
        val stack = ItemBuilder(Items.FEATHER)
            .customModelData(1005)
            .build()

        val modelData: CustomModelData? = stack.get(DataComponents.CUSTOM_MODEL_DATA)
        assertNotNull(modelData, "The item must have the CUSTOM_MODEL_DATA component.")
        assertEquals(1005, modelData?.value(), "The CustomModelData integer must match the input.")
    }

    @Test
    fun `test unbreakable toggle state`() {
        // Test setting to unbreakable
        val unbreakableStack = ItemBuilder(Items.SHIELD)
            .unbreakable(true)
            .build()

        assertTrue(unbreakableStack.has(DataComponents.UNBREAKABLE), "The item should have the UNBREAKABLE component.")

        // Test reverting / setting to breakable
        val breakableStack = ItemBuilder(Items.SHIELD)
            .unbreakable(true) // Set it
            .unbreakable(false) // Remove it
            .build()

        assertFalse(breakableStack.has(DataComponents.UNBREAKABLE), "The UNBREAKABLE component should be cleanly removed.")
    }
}