package com.novaco.luxapi.cobblemon.spawning

import com.cobblemon.mod.common.api.pokemon.PokemonSpecies
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.novaco.luxapi.commons.LuxAPI
import com.novaco.luxapi.commons.scheduler.LuxTask // 🌟 อย่าลืม Import คลาสนี้มาด้วยนะครับ
import net.minecraft.core.BlockPos
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.levelgen.Heightmap
import java.util.UUID
import kotlin.random.Random

object SwarmManager {

    private val activeSwarms = mutableMapOf<UUID, SwarmEvent>()

    private var currentTask: LuxTask? = null

    /**
     * Triggers a new Swarm Event.
     * @return The UUID of the created Swarm.
     */
    fun startSwarm(event: SwarmEvent): UUID {
        activeSwarms[event.id] = event
        ensureTaskRunning()
        return event.id
    }

    /**
     * Manually stops an active Swarm Event.
     */
    fun stopSwarm(id: UUID) {
        activeSwarms.remove(id)
    }

    /**
     * Bootstraps the repeating task using LuxAPI's Scheduler.
     */
    private fun ensureTaskRunning() {
        if (currentTask != null && currentTask?.isCancelled == false) return

        currentTask = LuxAPI.getScheduler().runRepeatingAsync(100L, 100L) {
            val server = LuxAPI.getService<MinecraftServer>() ?: return@runRepeatingAsync
            val swarmsIterator = activeSwarms.values.iterator()

            while (swarmsIterator.hasNext()) {
                val swarm = swarmsIterator.next()

                if (swarm.isExpired()) {
                    swarmsIterator.remove()
                    continue
                }

                LuxAPI.getScheduler().run {
                    processSwarmSpawning(server, swarm)
                }
            }

            if (activeSwarms.isEmpty()) {
                currentTask?.cancel()
                currentTask = null
            }
        }
    }

    /**
     * Handles the actual spawning logic safely on the Main Thread.
     */
    private fun processSwarmSpawning(server: MinecraftServer, swarm: SwarmEvent) {
        val level: ServerLevel = server.getLevel(swarm.dimension) ?: return

        swarm.activeEntities.removeIf { uuid ->
            val entity = level.getEntity(uuid)
            entity == null || !entity.isAlive
        }

        if (swarm.activeEntities.size >= swarm.maxActiveEntities) return

        val offsetX = Random.nextInt(-swarm.radius, swarm.radius)
        val offsetZ = Random.nextInt(-swarm.radius, swarm.radius)
        val targetX = swarm.centerPos.x + offsetX
        val targetZ = swarm.centerPos.z + offsetZ

        val targetY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, targetX, targetZ)
        val spawnPos = BlockPos(targetX, targetY, targetZ)

        if (level.getBlockState(spawnPos.below()).isAir) return

        val species = PokemonSpecies.getByName(swarm.speciesName.lowercase()) ?: return
        val pokemon = species.create()
        pokemon.level = Random.nextInt(swarm.minLevel, swarm.maxLevel + 1)

        val entity = PokemonEntity(level, pokemon)
        entity.setPos(spawnPos.x.toDouble() + 0.5, spawnPos.y.toDouble(), spawnPos.z.toDouble() + 0.5)

        if (level.addFreshEntity(entity)) {
            swarm.activeEntities.add(entity.uuid)
        }
    }
}