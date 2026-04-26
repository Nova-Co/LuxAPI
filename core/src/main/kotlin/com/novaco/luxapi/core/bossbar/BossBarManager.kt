package com.novaco.luxapi.core.bossbar

import net.minecraft.server.level.ServerBossEvent
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.LivingEntity
import java.util.UUID

/**
 * An automated tracking engine for entity-bound Boss Bars.
 * It automatically handles progress updates, player tracking (entering/leaving radius),
 * and boss death/removal cleanup.
 */
object BossBarManager {

    /** Internal data class to track the context of an active boss. */
    private data class TrackedBoss(
        val entity: LivingEntity,
        val bossBar: ServerBossEvent,
        val radius: Double
    )

    private val activeBosses = mutableMapOf<UUID, TrackedBoss>()

    /**
     * Registers a LivingEntity to be tracked by the manager.
     *
     * @param entity The boss entity.
     * @param bossBar The ServerBossEvent created via BossBarBuilder.
     * @param radius The tracking radius. Players within this radius will see the boss bar.
     */
    fun register(entity: LivingEntity, bossBar: ServerBossEvent, radius: Double = 50.0) {
        activeBosses[entity.uuid] = TrackedBoss(entity, bossBar, radius)
    }

    /**
     * Unregisters a boss and cleans up its boss bar for all viewers.
     */
    fun unregister(entityUuid: UUID) {
        val tracked = activeBosses.remove(entityUuid)
        tracked?.bossBar?.removeAllPlayers()
    }

    /**
     * Manually updates the progress of a specific boss bar.
     * Useful for external synchronizers (like Battle Engines) to force an update
     * when the physical entity's health hasn't synchronized yet.
     *
     * @param entityUuid The UUID of the tracked boss entity.
     * @param progress The health percentage to set (0.0f to 1.0f).
     */
    fun updateProgress(entityUuid: UUID, progress: Float) {
        activeBosses[entityUuid]?.bossBar?.progress = progress.coerceIn(0.0f, 1.0f)
    }

    /**
     * Must be called every server tick (e.g., via ServerTickEvents.END_SERVER_TICK in Fabric).
     * Automatically handles health updates and player tracking for all registered bosses.
     */
    fun tick() {
        if (activeBosses.isEmpty()) return

        val toRemove = mutableListOf<UUID>()

        for ((uuid, tracked) in activeBosses) {
            val entity = tracked.entity
            val bossBar = tracked.bossBar

            // Check if the boss is dead or removed from the world
            if (!entity.isAlive || entity.isRemoved) {
                toRemove.add(uuid)
                continue
            }

            // Update health progress
            val healthPercent = entity.health / entity.maxHealth
            bossBar.progress = healthPercent.coerceIn(0.0f, 1.0f)

            // Manage players in range
            val level = entity.level()
            if (!level.isClientSide) {
                // Find players within the tracking radius
                val searchBox = entity.boundingBox.inflate(tracked.radius)
                val playersInRange = level.getEntitiesOfClass(ServerPlayer::class.java, searchBox)

                // Add new players who entered the radius
                playersInRange.forEach { player ->
                    if (!bossBar.players.contains(player)) {
                        bossBar.addPlayer(player)
                    }
                }

                // Remove players who left the radius
                val currentPlayers = bossBar.players.toList()
                currentPlayers.forEach { player ->
                    if (!playersInRange.contains(player)) {
                        bossBar.removePlayer(player)
                    }
                }
            }
        }

        // Clean up dead/removed bosses
        toRemove.forEach { unregister(it) }
    }
}