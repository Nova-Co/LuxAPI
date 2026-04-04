package com.novaco.luxapi.commons.gui

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

/**
 * Mocking a basic GuiBuilder implementation to test logic bounds.
 */
class MockGuiBuilder(private var rows: Int = 3) {
    private val items = mutableMapOf<Int, String>()
    var title: String = "Chest"

    init {
        require(rows in 1..6) { "GUI rows must be between 1 and 6" }
    }

    fun setItem(slot: Int, item: String): MockGuiBuilder {
        val maxSlot = (rows * 9) - 1
        require(slot in 0..maxSlot) { "Slot $slot is out of bounds for a GUI with $rows rows" }
        items[slot] = item
        return this
    }

    fun getItems(): Map<Int, String> = items
}

class GuiBuilderTest {

    @Test
    fun `test gui builder row boundaries`() {
        // Valid rows (1 to 6)
        assertDoesNotThrow { MockGuiBuilder(3) }
        assertDoesNotThrow { MockGuiBuilder(6) }

        // Invalid rows (0 or > 6)
        assertThrows(IllegalArgumentException::class.java, { MockGuiBuilder(0) }, "0 rows should throw an exception.")
        assertThrows(IllegalArgumentException::class.java, { MockGuiBuilder(7) }, "7 rows should throw an exception.")
    }

    @Test
    fun `test item placement slot boundaries`() {
        val builder = MockGuiBuilder(3) // 3 rows = 27 slots (Index 0 to 26)

        assertDoesNotThrow { builder.setItem(0, "Apple") }
        assertDoesNotThrow { builder.setItem(26, "Diamond") }

        // Out of bounds
        assertThrows(IllegalArgumentException::class.java, { builder.setItem(-1, "Dirt") }, "Negative slots are invalid.")
        assertThrows(IllegalArgumentException::class.java, { builder.setItem(27, "Dirt") }, "Slot 27 is out of bounds for a 3-row GUI.")
    }
}