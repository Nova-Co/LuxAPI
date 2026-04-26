package com.novaco.luxapi.cobblemon.boss.minion

import com.cobblemon.mod.common.api.pokemon.PokemonProperties
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.novaco.luxapi.cobblemon.boss.aggro.BossAggroManager
import com.novaco.luxapi.cobblemon.fx.BossFxWrapper
import net.minecraft.server.level.ServerLevel
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.cos
import kotlin.math.sin

/**
 * Manages the summoning, tracking, and lifecycle of boss minions.
 */
object BossMinionManager {

    private val activeMinions = ConcurrentHashMap<UUID, MutableList<PokemonEntity>>()

    /**
     * Summons a specified amount of minion Pokemon radially around the boss entity.
     */
    fun summonMinions(bossEntity: PokemonEntity, propertyString: String, amount: Int, radius: Double = 3.0) {
        val level = bossEntity.level() as? ServerLevel ?: return
        val bossUuid = bossEntity.uuid

        val minionList = activeMinions.getOrPut(bossUuid) { mutableListOf() }

        val angleStep = (2 * Math.PI) / amount

        for (i in 0 until amount) {
            val angle = i * angleStep
            val spawnX = bossEntity.x + (cos(angle) * radius)
            val spawnY = bossEntity.y
            val spawnZ = bossEntity.z + (sin(angle) * radius)

            // Parse Pokemon properties (e.g., "species=zubat level=15")
            val pokemon = PokemonProperties.parse(propertyString).create()

            val minionEntity = PokemonEntity(level, pokemon)

            minionEntity.setPos(spawnX, spawnY, spawnZ)

            // Add identifying tags for AI and cleanup
            minionEntity.addTag("lux_is_minion")
            minionEntity.addTag("lux_boss_owner:$bossUuid")

            // Make them slightly smaller to distinguish them from the boss
            minionEntity.pokemon.scaleModifier = 0.8f

            if (level.addFreshEntity(minionEntity)) {
                minionList.add(minionEntity)
            }
        }
    }

    /**
     * Forces all active minions of a boss to pathfind and attack the boss's top aggro target.
     */
    fun updateMinionTargets(bossEntity: PokemonEntity) {
        val bossUuid = bossEntity.uuid
        val minions = activeMinions[bossUuid] ?: return

        // Fetch the player who dealt the most damage
        val topTarget = BossAggroManager.getTopTarget(bossEntity) ?: return

        minions.forEach { minion ->
            if (minion.isAlive) {
                // Set the Vanilla Minecraft target to force the entity to pathfind towards the player
                minion.target = topTarget
            }
        }
    }

    /**
     * Instantly removes all active minions associated with a specific boss UUID.
     */
    fun clearMinions(bossUuid: UUID) {
        val minions = activeMinions.remove(bossUuid) ?: return
        minions.forEach { minion ->
            if (minion.isAlive) {
                // Optional: Play a poof particle before discarding
                /*
                BossFxWrapper.playMoveParticle(
                    minion,
                    BossFxWrapper.MoveParticle.EXPLOSION_BOOM
                )
                */
                minion.discard()
            }
        }
    }

    /**
     * Removes a specific minion from the tracking list if it was killed by a player.
     */
    fun removeMinionFromTracking(bossUuid: UUID, minionEntity: PokemonEntity) {
        activeMinions[bossUuid]?.remove(minionEntity)
    }
}