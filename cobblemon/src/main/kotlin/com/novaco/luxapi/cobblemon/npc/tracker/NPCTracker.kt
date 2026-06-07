package com.novaco.luxapi.cobblemon.npc.tracker

import com.cobblemon.mod.common.entity.npc.NPCEntity
import net.minecraft.server.MinecraftServer
import net.minecraft.world.phys.Vec3
import java.util.UUID
import kotlin.math.atan2
import kotlin.math.sqrt

/**
 * An optimized tracker that manages dynamic "look at player" AI for specific LuxNPCs.
 * Instead of iterating through all entities, this tracker only processes NPCs that have been
 * explicitly registered, significantly improving performance on servers with many entities.
 */
object NPCTracker {

    private val trackedNPCs = mutableSetOf<UUID>()
    private const val TRACKING_RADIUS = 7.0

    /**
     * Registers an NPC to be processed by the tracker.
     * Only NPCs registered here will have their head rotation updated by this system.
     *
     * @param uuid The UUID of the NPCEntity to track.
     */
    fun register(uuid: UUID) {
        trackedNPCs.add(uuid)
    }

    /**
     * Unregisters an NPC from the tracking system.
     * This should be called when the NPC is removed or no longer needs this behavior.
     *
     * @param uuid The UUID of the NPCEntity to stop tracking.
     */
    fun unregister(uuid: UUID) {
        trackedNPCs.remove(uuid)
    }

    /**
     * The main tick function for the tracker. This must be called on every server tick.
     * It iterates through all tracked NPCs, finds them in the world, and updates their look direction.
     *
     * @param server The main MinecraftServer instance.
     */
    fun tick(server: MinecraftServer) {
        if (trackedNPCs.isEmpty()) return

        val deadEntities = mutableListOf<UUID>()

        for (uuid in trackedNPCs) {
            // Find the entity across all server levels (dimensions).
            val entity = server.allLevels
                .mapNotNull { it.getEntity(uuid) }
                .firstOrNull() as? NPCEntity

            // If the NPC is gone (unloaded or dead), schedule it for removal.
            if (entity == null || !entity.isAlive) {
                deadEntities.add(uuid)
                continue
            }

            // If the NPC is in battle, temporarily skip this behavior.
            if (entity.isInBattle()) continue

            processLookAtPlayer(entity)
        }

        // Clean up any references to NPCs that no longer exist.
        if (deadEntities.isNotEmpty()) {
            trackedNPCs.removeAll(deadEntities.toSet())
        }
    }

    /**
     * Calculates and applies the necessary head and body rotation for an NPC to look at the nearest player.
     *
     * @param npc The NPCEntity to update.
     */
    private fun processLookAtPlayer(npc: NPCEntity) {
        // Find the closest player within the defined radius.
        val nearestPlayer = npc.level().getNearestPlayer(
            npc.x, npc.y, npc.z, TRACKING_RADIUS, false
        )

        if (nearestPlayer == null) {
            // Optional: Implement logic here to make the NPC's head gradually return to a default forward-facing position.
            return
        }

        val targetVec: Vec3 = nearestPlayer.eyePosition
        val npcVec: Vec3 = npc.eyePosition

        val dx = targetVec.x - npcVec.x
        val dy = targetVec.y - npcVec.y
        val dz = targetVec.z - npcVec.z
        val horizontalDistance = sqrt(dx * dx + dz * dz)

        // Calculate the required yaw (horizontal) and pitch (vertical) rotations.
        val yaw = (atan2(dz, dx) * (180.0 / Math.PI)).toFloat() - 90.0f
        val pitch = (-(atan2(dy, horizontalDistance) * (180.0 / Math.PI))).toFloat()

        // Apply the new rotations to the NPC entity.
        npc.yHeadRot = yaw
        npc.xRot = pitch
        // Smoothly turn the body to partially face the target, creating a more natural look.
        val bodyYawDifference = yaw - npc.yBodyRot
        npc.yBodyRot += bodyYawDifference * 0.1f
    }
}