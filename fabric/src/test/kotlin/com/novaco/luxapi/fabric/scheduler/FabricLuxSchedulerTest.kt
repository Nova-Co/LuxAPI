package com.novaco.luxapi.fabric.scheduler

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class FabricLuxSchedulerTest {

    private lateinit var scheduler: FabricLuxScheduler

    @BeforeEach
    fun setup() {
        scheduler = FabricLuxScheduler()
    }

    /**
     * Helper function to manually trigger the private 'onServerTick' method
     * so we can simulate exactly how the server loops through tasks.
     */
    private fun simulateTick() {
        val tickMethod = FabricLuxScheduler::class.java.getDeclaredMethod("onServerTick")
        tickMethod.isAccessible = true
        tickMethod.invoke(scheduler)
    }

    @Test
    fun `test immediate synchronous execution`() {
        var executed = false

        val task = scheduler.run {
            executed = true
        }

        assertFalse(task.isAsync, "Task should be synchronous.")
        assertFalse(executed, "Task should not run until the tick happens.")

        simulateTick()

        assertTrue(executed, "Task should execute on the first tick.")
    }

    @Test
    fun `test delayed synchronous execution`() {
        var executionCount = 0

        // Schedule to run with a delay of 2 ticks
        scheduler.runLater(2) {
            executionCount++
        }

        simulateTick() // Tick 1: Delay decreases 2 -> 1
        assertEquals(0, executionCount, "Task should not execute yet.")

        simulateTick() // Tick 2: Delay decreases 1 -> 0
        assertEquals(0, executionCount, "Task is queued to run NEXT tick.")

        simulateTick() // Tick 3: Delay is 0 -> Executes!
        assertEquals(1, executionCount, "Task should execute after the delay finishes.")
    }

    @Test
    fun `test repeating task math and execution`() {
        var executionCount = 0

        // Delay 1 tick, repeat every 2 ticks
        val task = scheduler.runRepeating(1, 2) {
            executionCount++
        }

        simulateTick() // Tick 1: Delay 1 -> 0
        assertEquals(0, executionCount)

        simulateTick() // Tick 2: Delay is 0 -> Executes, Resets delay to 2
        assertEquals(1, executionCount)

        simulateTick() // Tick 3: Delay 2 -> 1
        assertEquals(1, executionCount)

        simulateTick() // Tick 4: Delay 1 -> 0
        assertEquals(1, executionCount)

        simulateTick() // Tick 5: Delay is 0 -> Executes, Resets delay to 2
        assertEquals(2, executionCount)

        task.cancel() // Clean up
    }

    @Test
    fun `test task cancellation removes it from execution queue`() {
        var executed = false

        val task = scheduler.runLater(1) {
            executed = true
        }

        // Cancel it immediately
        task.cancel()
        assertTrue(task.isCancelled, "Task state should be updated to cancelled.")

        simulateTick()
        simulateTick() // Even after required ticks, it should not run

        assertFalse(executed, "Cancelled tasks must never execute.")
    }

    @Test
    fun `test cancelAll wipes the active task queue`() {
        var counter = 0

        scheduler.runLater(1) { counter++ }
        scheduler.runRepeating(2, 5) { counter++ }

        scheduler.cancelAll()

        simulateTick()
        simulateTick()
        simulateTick()

        assertEquals(0, counter, "cancelAll must prevent all queued tasks from executing.")
    }

    @Test
    fun `test asynchronous execution triggers on a separate thread`() {
        // We use a CountDownLatch to safely wait for the async thread to finish before the test asserts
        val latch = CountDownLatch(1)
        var executedAsync = false
        var executedThreadName = ""

        val task = scheduler.runAsync {
            executedAsync = true
            executedThreadName = Thread.currentThread().name
            latch.countDown()
        }

        assertTrue(task.isAsync, "Task should be flagged as asynchronous.")

        simulateTick()

        // Wait up to 1 second for the async thread pool to finish the task
        val completedInTime = latch.await(1, TimeUnit.SECONDS)

        assertTrue(completedInTime, "The async task did not finish in time.")
        assertTrue(executedAsync, "The async task did not execute.")
        assertNotEquals(Thread.currentThread().name, executedThreadName, "The task must have executed on a different thread than the main test thread.")
    }
}