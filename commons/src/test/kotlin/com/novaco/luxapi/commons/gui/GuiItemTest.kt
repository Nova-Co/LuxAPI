package com.novaco.luxapi.commons.gui

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

/**
 * Simulating the GuiClickEvent for the test.
 */
class DummyClickEvent(val clickType: ClickType)

/**
 * A mock representation of your GuiItem.
 */
class MockGuiItem(val id: String) {
    var clickAction: ((DummyClickEvent) -> Unit)? = null

    fun onClick(action: (DummyClickEvent) -> Unit): MockGuiItem {
        this.clickAction = action
        return this
    }

    fun executeClick(event: DummyClickEvent) {
        clickAction?.invoke(event)
    }
}

class GuiItemTest {

    @Test
    fun `test gui item click action assignment and execution`() {
        val item = MockGuiItem("test_sword")
        var wasClicked = false
        var recordedClickType: ClickType? = null

        // Assign the click action using fluent builder pattern
        item.onClick { event ->
            wasClicked = true
            recordedClickType = event.clickType
        }

        // Simulate a player clicking the item
        val mockEvent = DummyClickEvent(ClickType.RIGHT)
        item.executeClick(mockEvent)

        assertTrue(wasClicked, "The click action lambda should be executed.")
        assertEquals(ClickType.RIGHT, recordedClickType, "The correct click type should be passed to the lambda.")
    }

    @Test
    fun `test gui item with no click action executes safely`() {
        val item = MockGuiItem("test_stone")

        // This should not throw a NullPointerException
        assertDoesNotThrow({
            item.executeClick(DummyClickEvent(ClickType.LEFT))
        }, "Executing a click on an item with no assigned action should be perfectly safe.")
    }
}