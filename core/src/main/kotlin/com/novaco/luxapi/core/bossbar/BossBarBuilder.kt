package com.novaco.luxapi.core.bossbar

import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerBossEvent
import net.minecraft.world.BossEvent

/**
 * A highly flexible builder for creating ServerBossEvents.
 * This can be used for World Bosses, Minigames, Event Timers, or any custom progress bars.
 */
class BossBarBuilder(private var title: String) {

    private var color: BossEvent.BossBarColor = BossEvent.BossBarColor.WHITE
    private var overlay: BossEvent.BossBarOverlay = BossEvent.BossBarOverlay.PROGRESS
    private var darkenScreen: Boolean = false
    private var playBossMusic: Boolean = false
    private var createWorldFog: Boolean = false
    private var initialProgress: Float = 1.0f

    /** Sets the color of the Boss Bar. */
    fun color(color: BossEvent.BossBarColor) = apply { this.color = color }

    /** Sets the visual division of the Boss Bar (e.g., PROGRESS, NOTCHED_6, NOTCHED_10). */
    fun overlay(overlay: BossEvent.BossBarOverlay) = apply { this.overlay = overlay }

    /** Darkens the sky for players viewing this Boss Bar. Great for dramatic boss fights. */
    fun darkenScreen(value: Boolean = true) = apply { this.darkenScreen = value }

    /** Plays the ominous end-boss ambient music. */
    fun playBossMusic(value: Boolean = true) = apply { this.playBossMusic = value }

    /** Creates a thick fog around the player, reducing render distance temporarily. */
    fun createWorldFog(value: Boolean = true) = apply { this.createWorldFog = value }

    /** Sets the initial fill percentage of the bar (0.0f to 1.0f). Defaults to 1.0f (Full). */
    fun progress(progress: Float) = apply { this.initialProgress = progress.coerceIn(0.0f, 1.0f) }

    /**
     * Builds and returns the configured ServerBossEvent.
     * Note: You must manually add players to this event for them to see it.
     */
    fun build(): ServerBossEvent {
        val bossEvent = ServerBossEvent(
            Component.literal(title),
            color,
            overlay
        )

        bossEvent.progress = initialProgress
        bossEvent.setDarkenScreen(darkenScreen)
        bossEvent.setPlayBossMusic(playBossMusic)
        bossEvent.setCreateWorldFog(createWorldFog)

        return bossEvent
    }
}

/**
 * Extension function for fluid DSL syntax.
 * Usage: val myBar = buildBossBar("My Boss") { color(RED); darkenScreen() }
 */
fun buildBossBar(title: String, init: BossBarBuilder.() -> Unit): ServerBossEvent {
    val builder = BossBarBuilder(title)
    builder.init()
    return builder.build()
}