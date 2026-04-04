package com.novaco.luxapi.commons.scheduler

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.concurrent.atomic.AtomicInteger

/**
 * A mock implementation of LuxTask for testing purposes.
 */
class MockLuxTask(override val id: Int, override val isAsync: Boolean) : LuxTask {
    override var isCancelled: Boolean = false

    override fun cancel() {
        isCancelled = true
    }
}

/**
 * A mock implementation of LuxScheduler.
 * Simulates Minecraft ticks by converting them to milliseconds (1 tick = 50ms).
 */
class MockLuxScheduler : LuxScheduler {
    private var taskIdCounter = 0
    private val activeTasks = mutableListOf<MockLuxTask>()

    override fun run(runnable: Runnable): LuxTask {
        val task = MockLuxTask(taskIdCounter++, false)
        activeTasks.add(task)
        if (!task.isCancelled) runnable.run()
        return task
    }

    override fun runAsync(runnable: Runnable): LuxTask {
        val task = MockLuxTask(taskIdCounter++, true)
        activeTasks.add(task)
        Thread { if (!task.isCancelled) runnable.run() }.start()
        return task
    }

    override fun runLater(delay: Long, runnable: Runnable): LuxTask {
        val task = MockLuxTask(taskIdCounter++, false)
        activeTasks.add(task)

        // Simulate Minecraft ticks (1 tick = 50ms)
        Thread {
            Thread.sleep(delay * 50L)
            if (!task.isCancelled) runnable.run()
        }.start()

        return task
    }

    override fun runLaterAsync(delay: Long, runnable: Runnable): LuxTask = runLater(delay, runnable)

    override fun runRepeating(delay: Long, period: Long, runnable: Runnable): LuxTask {
        throw NotImplementedError("Repeating tasks are not tested in this mock.")
    }

    override fun runRepeatingAsync(delay: Long, period: Long, runnable: Runnable): LuxTask {
        throw NotImplementedError("Repeating tasks are not tested in this mock.")
    }

    override fun cancelAll() {
        activeTasks.forEach { it.cancel() }
        activeTasks.clear()
    }
}

class LuxSchedulerTest {

    private lateinit var scheduler: LuxScheduler

    @BeforeEach
    fun setup() {
        scheduler = MockLuxScheduler()
    }

    @Test
    fun `test synchronous task execution`() {
        var executed = false

        scheduler.run { executed = true }

        assertTrue(executed, "Synchronous task should execute immediately on the caller thread.")
    }

    @Test
    fun `test delayed task execution using ticks`() {
        val counter = AtomicInteger(0)

        // Schedule a task with a 2-tick delay (approx 100ms)
        scheduler.runLater(2L) {
            counter.incrementAndGet()
        }

        // Immediately check: should still be 0
        assertEquals(0, counter.get(), "Task should not execute before the tick delay has passed.")

        // Wait for the delay (100ms) + a 50ms buffer for thread switching
        Thread.sleep(150L)

        assertEquals(1, counter.get(), "Task should have executed after the simulated tick delay.")
    }

    @Test
    fun `test task cancellation`() {
        val counter = AtomicInteger(0)

        // Schedule a task with a 4-tick delay (approx 200ms)
        val task = scheduler.runLater(4L) {
            counter.incrementAndGet()
        }

        // Cancel it immediately before it fires
        task.cancel()

        assertTrue(task.isCancelled, "Task state should accurately reflect cancellation.")

        // Wait beyond the original delay time
        Thread.sleep(250L)

        assertEquals(0, counter.get(), "A cancelled task must never execute its runnable payload.")
    }

    @Test
    fun `test cancel all tasks`() {
        val task1 = scheduler.runLater(10L) {}
        val task2 = scheduler.runLater(10L) {}

        scheduler.cancelAll()

        assertTrue(task1.isCancelled, "Task 1 should be cancelled.")
        assertTrue(task2.isCancelled, "Task 2 should be cancelled.")
    }
}