package com.novaco.luxapi.commons.event

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

/**
 * A simple dummy event used exclusively for testing the EventBus.
 */
class DummyEvent(var wasHandled: Boolean = false) : LuxEvent

/**
 * A mock listener class containing a subscribed method.
 */
class DummyListener {
    @Subscribe
    fun onDummyEvent(event: DummyEvent) {
        event.wasHandled = true
    }
}

class EventBusTest {

    @BeforeEach
    fun setup() {
        // Clear all listeners before each test to guarantee isolation
        EventBus.clear()
    }

    @Test
    fun `test event subscription and dispatch routing`() {
        val listener = DummyListener()

        // Register the listener to the bus
        EventBus.register(listener)

        // Fire the event (Fixed: using fire() instead of post())
        val event = DummyEvent()
        EventBus.fire(event)

        // Verify the event was intercepted and modified
        assertTrue(event.wasHandled, "The EventBus should correctly route the event to the subscribed method.")
    }

    @Test
    fun `test unregistered listeners do not receive events`() {
        val listener = DummyListener()
        EventBus.register(listener)

        // Immediately unregister
        EventBus.unregister(listener)

        val event = DummyEvent()
        EventBus.fire(event)

        assertFalse(event.wasHandled, "Unregistered listeners should absolutely not receive events.")
    }
}