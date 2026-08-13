package com.novaco.luxapi.cobblemon.drop

import com.cobblemon.mod.common.api.drop.DropTable
import com.cobblemon.mod.common.api.drop.ItemDropEntry
import net.minecraft.SharedConstants
import net.minecraft.server.Bootstrap
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test

class DropManagerTest {

    companion object {
        @JvmStatic
        @BeforeAll
        fun bootstrapMinecraft() {
            SharedConstants.tryDetectVersion()
            Bootstrap.bootStrap()
        }
    }

    @Test
    fun `addItemDrop appends a valid entry to the table`() {
        val table = DropTable()

        val result = DropManager.addItemDrop(table, "cobblemon:poke_ball", percentage = 50F, quantity = 2, maxSelectableTimes = 3)

        assertTrue(result)
        assertEquals(1, table.entries.size)
        val entry = table.entries[0] as ItemDropEntry
        assertEquals("cobblemon:poke_ball", entry.item.toString())
        assertEquals(50F, entry.percentage)
        assertEquals(2, entry.quantity)
        assertEquals(3, entry.maxSelectableTimes)
    }

    @Test
    fun `addItemDrop rejects an invalid item id without touching the table`() {
        val table = DropTable()

        val result = DropManager.addItemDrop(table, "not a valid id")

        assertFalse(result)
        assertTrue(table.entries.isEmpty())
    }

    @Test
    fun `clearDrops empties the table`() {
        val table = DropTable()
        DropManager.addItemDrop(table, "cobblemon:poke_ball")

        DropManager.clearDrops(table)

        assertTrue(table.entries.isEmpty())
    }
}
