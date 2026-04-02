package com.novaco.luxapi.fabric.scheduler

import com.novaco.luxapi.commons.scheduler.LuxScheduler
import com.novaco.luxapi.commons.scheduler.LuxTask
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger

/**
 * The Fabric implementation of the LuxScheduler.
 * Taps into the native Fabric tick loop to process synchronous tasks,
 * and utilizes a cached thread pool for asynchronous execution.
 */
class FabricLuxScheduler : LuxScheduler {

    // A thread pool that reuses previously constructed threads when they are available.
    private val asyncPool = Executors.newCachedThreadPool()

    // Thread-safe map to store all active tasks.
    private val activeTasks = ConcurrentHashMap<Int, TaskData>()

    // Generates unique IDs for every task securely across threads.
    private val taskIdGenerator = AtomicInteger(0)

    /**
     * Initializes the scheduler by hooking into the Fabric server tick event.
     */
    fun registerTickListener() {
        ServerTickEvents.END_SERVER_TICK.register { _ ->
            onServerTick()
        }
    }

    /**
     * Processes all active tasks. Called exactly once every Minecraft tick (20 times per second).
     */
    private fun onServerTick() {
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
        val luxTask = FabricLuxTask(taskId, isAsync)
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
        val task: FabricLuxTask,
        var currentDelay: Long,
        val period: Long,
        val runnable: Runnable
    )
}