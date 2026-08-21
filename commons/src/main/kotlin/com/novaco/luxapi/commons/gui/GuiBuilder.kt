package com.novaco.luxapi.commons.gui

import com.novaco.luxapi.commons.LuxAPI
import com.novaco.luxapi.commons.scheduler.LuxTask

/**
 * A builder pattern for constructing instances of LuxGui efficiently.
 * Supports automated filling and complex layout configurations.
 */
abstract class GuiBuilder {

    protected var title: String = "Menu"
    protected var rows: Int = 3
    protected val items = mutableMapOf<Int, GuiItem>()

    /**
     * A slot registered via [setAnimatedItem], not yet attached to a built [Gui].
     */
    private data class AnimatedSlot(
        val initialDelay: Long,
        val period: Long,
        val async: Boolean,
        val supplier: () -> GuiItem
    )

    private val animatedItems = mutableMapOf<Int, AnimatedSlot>()

    /**
     * Sets the display title of the graphical user interface.
     */
    open fun title(title: String): GuiBuilder {
        this.title = title
        return this
    }

    /**
     * Sets the number of rows for the graphical user interface.
     */
    open fun rows(rows: Int): GuiBuilder {
        require(rows in 1..6) { "Rows must be between 1 and 6" }
        this.rows = rows
        return this
    }

    /**
     * Places a specific GUI item into the specified slot.
     */
    open fun setItem(slot: Int, item: GuiItem): GuiBuilder {
        val maxSlot = rows * 9
        if (slot in 0 until maxSlot) {
            this.items[slot] = item
        }
        return this
    }

    /**
     * Fills the entire border of the GUI with a background item.
     * Calculated based on the current number of rows.
     */
    open fun fillBorder(item: GuiItem): GuiBuilder {
        val totalSlots = rows * 9
        for (i in 0 until totalSlots) {
            val isTopRow = i < 9
            val isBottomRow = i >= totalSlots - 9
            val isLeftColumn = i % 9 == 0
            val isRightColumn = i % 9 == 8

            if (isTopRow || isBottomRow || isLeftColumn || isRightColumn) {
                if (!items.containsKey(i)) {
                    setItem(i, item)
                }
            }
        }
        return this
    }

    /**
     * Fills all empty slots in the current GUI layout with a specific item.
     */
    open fun fillEmpty(item: GuiItem): GuiBuilder {
        for (i in 0 until (rows * 9)) {
            if (!items.containsKey(i)) {
                setItem(i, item)
            }
        }
        return this
    }

    /**
     * Places a slot that updates itself on a repeating schedule, without the caller needing to
     * run its own scheduler loop — e.g. a live leaderboard or auction menu slot.
     *
     * @param slot The target slot.
     * @param period Ticks between updates.
     * @param initialDelay Ticks before the first update. Defaults to [period].
     * @param async Whether updates run on [com.novaco.luxapi.commons.scheduler.LuxScheduler.runRepeatingAsync]
     * instead of the main thread. Only safe if [supplier] does no platform/inventory API calls itself.
     * @param supplier Produces the item to display each time the slot updates.
     */
    open fun setAnimatedItem(
        slot: Int,
        period: Long,
        initialDelay: Long = period,
        async: Boolean = false,
        supplier: () -> GuiItem
    ): GuiBuilder {
        val maxSlot = rows * 9
        if (slot in 0 until maxSlot) {
            setItem(slot, supplier())
            animatedItems[slot] = AnimatedSlot(initialDelay, period, async, supplier)
        }
        return this
    }

    /**
     * Starts the scheduled updates for any slot registered via [setAnimatedItem], against the
     * now-built [gui]. Each platform's `build()` must call this after constructing its [Gui]
     * instance — it's not automatic, since a builder has nothing to schedule against until then.
     * Each animated slot's task self-cancels via [Gui.hasViewers] once nobody's watching, so a
     * closed GUI's animation doesn't run forever. It also self-cancels if [Gui.generation] has
     * moved on since this task started, meaning [gui] now represents a different logical screen
     * (via another `Gui`'s inventory-reuse open) than the one this animation was built for.
     */
    protected fun startAnimations(gui: Gui) {
        if (animatedItems.isEmpty()) return
        val scheduler = LuxAPI.getScheduler()
        val startGeneration = gui.generation

        animatedItems.forEach { (slot, spec) ->
            var task: LuxTask? = null
            val tick = Runnable {
                if (!gui.hasViewers() || gui.generation != startGeneration) {
                    task?.cancel()
                    return@Runnable
                }
                gui.setItem(slot, spec.supplier())
                gui.refreshAll()
            }
            task = if (spec.async) {
                scheduler.runRepeatingAsync(spec.initialDelay, spec.period, tick)
            } else {
                scheduler.runRepeating(spec.initialDelay, spec.period, tick)
            }
        }
    }

    /**
     * Finalizes the configuration and builds the platform-specific graphical user interface.
     */
    abstract fun build(): Gui
}