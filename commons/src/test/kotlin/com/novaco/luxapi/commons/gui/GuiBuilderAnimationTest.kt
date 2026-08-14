package com.novaco.luxapi.commons.gui

import com.novaco.luxapi.commons.LuxAPI
import com.novaco.luxapi.commons.player.LuxPlayer
import com.novaco.luxapi.commons.scheduler.LuxScheduler
import com.novaco.luxapi.commons.scheduler.LuxTask
import com.novaco.luxapi.commons.scheduler.MockLuxTask
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

/**
 * A fake [Gui] that records what [setAnimatedItem]'s scheduled tick does to it, without any
 * real platform/inventory backing.
 */
class FakeAnimatedGui(initial: Map<Int, GuiItem>) : Gui {
    val itemsMap = initial.toMutableMap()
    var viewersPresent = true
    var refreshAllCallCount = 0
        private set

    override fun open(player: LuxPlayer) {}
    override fun close(player: LuxPlayer) {}
    override fun setItem(slot: Int, item: GuiItem) { itemsMap[slot] = item }
    override fun getItem(slot: Int): GuiItem? = itemsMap[slot]
    override fun refresh(player: LuxPlayer) {}
    override fun refreshAll() { refreshAllCallCount++ }
    override fun hasViewers(): Boolean = viewersPresent
}

/**
 * A fake [LuxScheduler] that captures a `runRepeating`/`runRepeatingAsync` call instead of
 * actually scheduling anything, so a test can fire the captured tick manually and assert on it.
 */
class CapturingScheduler : LuxScheduler {
    data class Captured(val initialDelay: Long, val period: Long, val async: Boolean, val runnable: Runnable, val task: LuxTask)

    private var idCounter = 0
    val capturedRepeats = mutableListOf<Captured>()

    override fun run(runnable: Runnable): LuxTask = MockLuxTask(idCounter++, false)
    override fun runAsync(runnable: Runnable): LuxTask = MockLuxTask(idCounter++, true)
    override fun runLater(delay: Long, runnable: Runnable): LuxTask = MockLuxTask(idCounter++, false)
    override fun runLaterAsync(delay: Long, runnable: Runnable): LuxTask = MockLuxTask(idCounter++, true)

    override fun runRepeating(delay: Long, period: Long, runnable: Runnable): LuxTask {
        val task = MockLuxTask(idCounter++, false)
        capturedRepeats.add(Captured(delay, period, false, runnable, task))
        return task
    }

    override fun runRepeatingAsync(delay: Long, period: Long, runnable: Runnable): LuxTask {
        val task = MockLuxTask(idCounter++, true)
        capturedRepeats.add(Captured(delay, period, true, runnable, task))
        return task
    }

    override fun cancelAll() {
        capturedRepeats.forEach { (it.task as MockLuxTask).cancel() }
    }
}

/**
 * Exposes [GuiBuilder]'s protected [items]/[startAnimations] for the test to drive directly,
 * against a [FakeAnimatedGui] instead of a real platform Gui.
 */
class TestGuiBuilder : GuiBuilder() {
    lateinit var builtGui: FakeAnimatedGui

    fun itemsSnapshot(): Map<Int, GuiItem> = items.toMap()

    override fun build(): Gui {
        builtGui = FakeAnimatedGui(items)
        startAnimations(builtGui)
        return builtGui
    }
}

class GuiBuilderAnimationTest {

    private lateinit var scheduler: CapturingScheduler
    private lateinit var originalProvider: () -> LuxScheduler

    @BeforeEach
    fun setup() {
        originalProvider = LuxAPI.schedulerProvider
        scheduler = CapturingScheduler()
        LuxAPI.schedulerProvider = { scheduler }
    }

    @AfterEach
    fun tearDown() {
        LuxAPI.schedulerProvider = originalProvider
    }

    @Test
    fun `test setAnimatedItem seeds the slot immediately, before any tick fires`() {
        val builder = TestGuiBuilder()
        builder.rows(1)
        builder.setAnimatedItem(slot = 0, period = 20L) { GuiItem("minecraft:clock") }

        assertEquals("minecraft:clock", builder.itemsSnapshot()[0]?.material)
    }

    @Test
    fun `test an out-of-bounds slot registers no animation`() {
        val builder = TestGuiBuilder()
        builder.rows(1)
        builder.setAnimatedItem(slot = 99, period = 20L) { GuiItem("minecraft:clock") }
        builder.build()

        assertTrue(scheduler.capturedRepeats.isEmpty(), "An out-of-range slot should never reach the scheduler.")
    }

    @Test
    fun `test initialDelay defaults to period when not specified`() {
        val builder = TestGuiBuilder()
        builder.rows(1)
        builder.setAnimatedItem(slot = 0, period = 40L) { GuiItem("minecraft:clock") }
        builder.build()

        val captured = scheduler.capturedRepeats.single()
        assertEquals(40L, captured.period)
        assertEquals(40L, captured.initialDelay)
    }

    @Test
    fun `test async flag is forwarded to runRepeatingAsync`() {
        val builder = TestGuiBuilder()
        builder.rows(1)
        builder.setAnimatedItem(slot = 0, period = 20L, async = true) { GuiItem("minecraft:clock") }
        builder.build()

        assertTrue(scheduler.capturedRepeats.single().async)
    }

    @Test
    fun `test a fired tick updates the built gui and refreshes every viewer`() {
        var callCount = 0
        val builder = TestGuiBuilder()
        builder.rows(1)
        builder.setAnimatedItem(slot = 0, period = 20L) {
            callCount++
            GuiItem("minecraft:clock_$callCount")
        }
        builder.build()

        scheduler.capturedRepeats.single().runnable.run()

        assertEquals("minecraft:clock_2", builder.builtGui.itemsMap[0]?.material, "The tick should call the supplier again, not reuse the seeded item.")
        assertEquals(1, builder.builtGui.refreshAllCallCount)
    }

    @Test
    fun `test a tick self-cancels once the gui has no viewers, without refreshing`() {
        val builder = TestGuiBuilder()
        builder.rows(1)
        builder.setAnimatedItem(slot = 0, period = 20L) { GuiItem("minecraft:clock") }
        builder.build()
        builder.builtGui.viewersPresent = false

        val captured = scheduler.capturedRepeats.single()
        captured.runnable.run()

        assertTrue(captured.task.isCancelled, "The task should cancel itself once nobody is watching.")
        assertEquals(0, builder.builtGui.refreshAllCallCount, "An empty-viewer tick shouldn't bother refreshing anyone.")
    }
}
