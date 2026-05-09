package com.novaco.luxapi.cobblemon.npc.tracker

import com.cobblemon.mod.common.entity.npc.NPCEntity
import net.minecraft.server.MinecraftServer
import net.minecraft.world.phys.Vec3
import java.util.UUID
import kotlin.math.atan2
import kotlin.math.sqrt

/**
 * A highly optimized tracker that handles dynamic head-rotation AI for LuxNPCs.
 * Only processes NPCs explicitly registered to look at players.
 */
object LuxNPCTracker {

    private val trackedNPCs = mutableSetOf<UUID>()
    private const val TRACKING_RADIUS = 7.0

    /**
     * Registers an NPC to actively look at nearby players.
     */
    fun register(uuid: UUID) {
        trackedNPCs.add(uuid)
    }

    /**
     * Unregisters an NPC from the tracking system.
     */
    fun unregister(uuid: UUID) {
        trackedNPCs.remove(uuid)
    }

    /**
     * MUST be called during a Server Tick Event (e.g., via LuxScheduler).
     * Processes all registered NPCs and updates their look vectors.
     */
    fun tick(server: MinecraftServer) {
        if (trackedNPCs.isEmpty()) return

        val deadEntities = mutableListOf<UUID>()

        for (uuid in trackedNPCs) {
            // Locate the entity across all dimensions
            val entity = server.allLevels
                .mapNotNull { it.getEntity(uuid) }
                .firstOrNull() as? NPCEntity

            // Clean up if the NPC was deleted or unloaded
            if (entity == null || !entity.isAlive) {
                deadEntities.add(uuid)
                continue
            }

            processLookAtPlayer(entity)
        }

        // Cleanup dead references
        if (deadEntities.isNotEmpty()) {
            trackedNPCs.removeAll(deadEntities.toSet())
        }
    }

    /**
     * Calculates the yaw and pitch required for the NPC to look at the nearest player.
     */
    private fun processLookAtPlayer(npc: NPCEntity) {
        // Find the nearest player within the tracking radius
        val nearestPlayer = npc.level().getNearestPlayer(
            npc.x, npc.y, npc.z, TRACKING_RADIUS, false
        )

        if (nearestPlayer == null) {
            // Gradually return head to default position here
            return
        }

        val targetVec: Vec3 = nearestPlayer.eyePosition
        val npcVec: Vec3 = npc.eyePosition

        val dx = targetVec.x - npcVec.x
        val dy = targetVec.y - npcVec.y
        val dz = targetVec.z - npcVec.z
        val horizontalDistance = sqrt(dx * dx + dz * dz)

        // Calculate new rotations
        val yaw = (atan2(dz, dx) * (180.0 / Math.PI)).toFloat() - 90.0f
        val pitch = (-(atan2(dy, horizontalDistance) * (180.0 / Math.PI))).toFloat()

        // Apply to the NPC
        npc.yHeadRot = yaw
        npc.xRot = pitch
        val bodyYawDifference = yaw - npc.yBodyRot
        npc.yBodyRot += bodyYawDifference * 0.1f
    }
}