package com.novaco.luxapi.commons.event

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

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

/**
 * A listener that increments a shared counter, used to detect lost
 * registrations under concurrent access.
 */
class CountingListener(private val counter: AtomicInteger) {
    @Subscribe
    fun onDummyEvent(event: DummyEvent) {
        counter.incrementAndGet()
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

    @Test
    fun `test concurrent registration from multiple threads does not lose listeners`() {
        val threadCount = 200
        val counter = AtomicInteger(0)
        val listeners = (1..threadCount).map { CountingListener(counter) }
        val executor = Executors.newFixedThreadPool(16)
        val latch = CountDownLatch(threadCount)

        listeners.forEach { listener ->
            executor.submit {
                try {
                    EventBus.register(listener)
                } finally {
                    latch.countDown()
                }
            }
        }

        assertTrue(latch.await(10, TimeUnit.SECONDS), "All registrations should complete without deadlock")
        executor.shutdown()

        EventBus.fire(DummyEvent())

        assertEquals(
            threadCount,
            counter.get(),
            "Every concurrently-registered listener must receive the event exactly once " +
                "-- a race in EventBus's backing collections silently drops registrations."
        )
    }
}