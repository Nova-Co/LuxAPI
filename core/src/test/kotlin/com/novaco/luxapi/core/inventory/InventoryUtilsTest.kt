package com.novaco.luxapi.core.inventory

import net.minecraft.SharedConstants
import net.minecraft.server.Bootstrap
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*

class InventoryUtilsTest {

    companion object {
        @JvmStatic
        @BeforeAll
        fun initMinecraft() {
            SharedConstants.tryDetectVersion()
            Bootstrap.bootStrap()
        }
    }

    /**
     * Creates a mocked player but attaches a REAL Minecraft Inventory to them.
     * This bypasses Mockito's inability to mock public fields like 'inventory.items'.
     */
    private fun createMockPlayerWithInventory(): Pair<Player, Inventory> {
        val mockPlayer = mock(Player::class.java)
        val realInventory = Inventory(mockPlayer)

        // Whenever the plugin asks for the player's inventory, give them the real one
        `when`(mockPlayer.inventory).thenReturn(realInventory)

        return Pair(mockPlayer, realInventory)
    }

    @Test
    fun `test has free space`() {
        val (player, inventory) = createMockPlayerWithInventory()

        // 1. A fresh inventory is empty, so it should have free space
        assertTrue(InventoryUtils.hasFreeSpace(player), "A new inventory should have free space.")

        // 2. Fill the entire inventory to simulate a full state
        for (i in 0 until inventory.containerSize) {
            inventory.setItem(i, ItemStack(Items.DIRT, 64))
        }

        assertFalse(InventoryUtils.hasFreeSpace(player), "A completely filled inventory should return false.")
    }

    @Test
    fun `test count free slots`() {
        val (player, inventory) = createMockPlayerWithInventory()

        // Place 2 items in the inventory
        inventory.setItem(0, ItemStack(Items.APPLE))
        inventory.setItem(1, ItemStack(Items.APPLE))

        // The main inventory has 36 slots. We filled 2, so 34 should be free.
        val freeSlots = InventoryUtils.countFreeSlots(player)
        assertEquals(34, freeSlots, "Should correctly calculate the remaining EMPTY slots in the main inventory.")
    }

    @Test
    fun `test give item safely drops on ground if full`() {
        val (player, inventory) = createMockPlayerWithInventory()
        val reward = ItemStack(Items.DIAMOND, 64)

        // Fill the inventory so the item cannot be added
        for (i in 0 until inventory.containerSize) {
            inventory.setItem(i, ItemStack(Items.DIRT, 64))
        }

        InventoryUtils.giveItemSafely(player, reward)

        // Verify that because it failed to add, it forced the player to drop it on the ground
        verify(player).drop(reward, false)
    }

    @Test
    fun `test give item safely does not drop if added successfully`() {
        val (player, inventory) = createMockPlayerWithInventory()
        val reward = ItemStack(Items.DIAMOND, 64)

        InventoryUtils.giveItemSafely(player, reward)

        // Verify that the item was physically placed into the first slot
        assertEquals(Items.DIAMOND, inventory.getItem(0).item, "The first slot should contain the Diamond.")
        assertEquals(64, inventory.getItem(0).count, "The first slot should have exactly 64 items.")

        // Verify that the drop method was NEVER called
        verify(player, never()).drop(any(ItemStack::class.java), anyBoolean())
    }

    @Test
    fun `test has item amount`() {
        val (player, inventory) = createMockPlayerWithInventory()

        // Setup the inventory
        inventory.setItem(0, ItemStack(Items.APPLE, 10))
        inventory.setItem(1, ItemStack(Items.APPLE, 5))
        inventory.setItem(2, ItemStack(Items.STICK, 64)) // Irrelevant item

        assertTrue(InventoryUtils.hasItemAmount(player, Items.APPLE, 15), "Player has exactly 15 apples.")
        assertTrue(InventoryUtils.hasItemAmount(player, Items.APPLE, 5), "Player has more than 5 apples.")
        assertFalse(InventoryUtils.hasItemAmount(player, Items.APPLE, 20), "Player does not have 20 apples.")
        assertFalse(InventoryUtils.hasItemAmount(player, Items.DIAMOND, 1), "Player has no diamonds.")
    }

    @Test
    fun `test consume item reduces correct amounts`() {
        val (player, inventory) = createMockPlayerWithInventory()

        // Setup the player with 15 Gold Ingots split across two slots
        inventory.setItem(0, ItemStack(Items.GOLD_INGOT, 10))
        inventory.setItem(1, ItemStack(Items.GOLD_INGOT, 5))

        // Attempt to consume 12 Gold Ingots
        val success = InventoryUtils.consumeItem(player, Items.GOLD_INGOT, 12)

        assertTrue(success, "Should successfully consume the items.")

        // Slot 0 had 10, so it should be completely emptied (is empty)
        assertTrue(inventory.getItem(0).isEmpty, "The first slot should be completely empty.")

        // Slot 1 had 5. We needed 2 more, so it should shrink by 2, leaving 3.
        assertEquals(3, inventory.getItem(1).count, "The second stack should be shrunk by the remaining required amount.")
    }
}