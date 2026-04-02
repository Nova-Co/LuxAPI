package com.novaco.luxapi.neoforge.scheduler

import com.novaco.luxapi.commons.scheduler.LuxScheduler
import com.novaco.luxapi.commons.scheduler.LuxTask
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.neoforge.common.NeoForge
import net.neoforged.neoforge.event.tick.ServerTickEvent
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger

/**
 * The NeoForge implementation of the LuxScheduler.
 * Taps into the native NeoForge tick loop to process synchronous tasks,
 * and utilizes a cached thread pool for asynchronous execution.
 */
class NeoForgeLuxScheduler : LuxScheduler {

    // A thread pool that reuses previously constructed threads when they are available.
    private val asyncPool = Executors.newCachedThreadPool()

    // Thread-safe map to store all active tasks.
    private val activeTasks = ConcurrentHashMap<Int, TaskData>()

    // Generates unique IDs for every task securely across threads.
    private val taskIdGenerator = AtomicInteger(0)

    /**
     * Initializes the scheduler by hooking into the NeoForge event bus.
     */
    fun register() {
        NeoForge.EVENT_BUS.register(this)
    }

    /**
     * Processes all active tasks. Called exactly once every Minecraft tick (20 times per second)
     * at the end of the server tick cycle.
     */
    @SubscribeEvent
    fun onServerTick(event: ServerTickEvent.Post) {
        if (activeTasks.isEmpty()) return

        val iterator = activeTasks.values.iterator()
        while (iterator.hasNext()) {
            val data = iterator.next()

            // 1. Remove cancelled tasks
            if (data.task.isCancelled) {
                iterator.remove()
                continue
            }

            // 2. Countdown the delay
            if (data.currentDelay > 0) {
                data.currentDelay--
                continue
            }

            // 3. Execute the task when delay reaches 0
            if (data.task.isAsync) {
                asyncPool.execute(data.runnable)
            } else {
                try {
                    data.runnable.run()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            // 4. Handle repeating logic
            if (data.period > 0) {
                data.currentDelay = data.period // Reset the delay to the period
            } else {
                iterator.remove() // One-time task, remove it after running
            }
        }
    }

    private fun schedule(delay: Long, period: Long, isAsync: Boolean, runnable: Runnable): LuxTask {
        val taskId = taskIdGenerator.incrementAndGet()
        val luxTask = NeoForgeLuxTask(taskId, isAsync)
        val taskData = TaskData(luxTask, delay, period, runnable)

        activeTasks[taskId] = taskData
        return luxTask
    }

    override fun run(runnable: Runnable): LuxTask = schedule(0, 0, false, runnable)

    override fun runAsync(runnable: Runnable): LuxTask = schedule(0, 0, true, runnable)

    override fun runLater(delay: Long, runnable: Runnable): LuxTask = schedule(delay, 0, false, runnable)

    override fun runLaterAsync(delay: Long, runnable: Runnable): LuxTask = schedule(delay, 0, true, runnable)

    override fun runRepeating(delay: Long, period: Long, runnable: Runnable): LuxTask = schedule(delay, period, false, runnable)

    override fun runRepeatingAsync(delay: Long, period: Long, runnable: Runnable): LuxTask = schedule(delay, period, true, runnable)

    override fun cancelAll() {
        activeTasks.values.forEach { it.task.cancel() }
        activeTasks.clear()
    }

    /**
     * Internal data class to hold the execution state of a task.
     */
    private data class TaskData(
        val task: NeoForgeLuxTask,
        var currentDelay: Long,
        val period: Long,
        val runnable: Runnable
    )
}