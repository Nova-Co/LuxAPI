package com.novaco.luxapi.cobblemon.spawning

import com.cobblemon.mod.common.api.pokemon.PokemonSpecies
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.novaco.luxapi.commons.LuxAPI
import com.novaco.luxapi.commons.scheduler.LuxTask
import net.minecraft.core.BlockPos
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.levelgen.Heightmap
import java.util.UUID
import kotlin.random.Random

/**
 * Manages the lifecycle of all active Pokémon swarm events.
 * This object is responsible for starting, stopping, and processing the spawning logic
 * for each [SwarmEvent] using an efficient, scheduled task.
 */
object SwarmManager {

    private val activeSwarms = mutableMapOf<UUID, SwarmEvent>()
    private var currentTask: LuxTask? = null

    /**
     * Starts a new swarm event and begins the spawning process.
     *
     * @param event The [SwarmEvent] configuration object.
     * @return The unique ID of the created swarm, which can be used to stop it later.
     */
    fun startSwarm(event: SwarmEvent): UUID {
        activeSwarms[event.id] = event
        ensureTaskRunning()
        return event.id
    }

    /**
     * Manually stops and removes an active swarm event.
     * Any Pokémon already spawned by the swarm will remain, but no new ones will appear.
     *
     * @param id The unique ID of the swarm to stop.
     */
    fun stopSwarm(id: UUID) {
        activeSwarms.remove(id)
    }

    /**
     * Ensures that the background processing task is running if there are active swarms.
     * If the task is not running, it starts a new repeating asynchronous task that periodically
     * triggers the spawning logic for all active swarms. The task will automatically stop
     * itself when no swarms are left.
     */
    private fun ensureTaskRunning() {
        // Do nothing if the task is already active.
        if (currentTask != null && currentTask?.isCancelled == false) return

        // Schedule a new task that runs every 5 seconds (100 ticks).
        currentTask = LuxAPI.getScheduler().runRepeatingAsync(100L, 100L) {
            val server = LuxAPI.getService<MinecraftServer>() ?: return@runRepeatingAsync
            val swarmsIterator = activeSwarms.values.iterator()

            while (swarmsIterator.hasNext()) {
                val swarm = swarmsIterator.next()

                // Clean up expired swarms.
                if (swarm.isExpired()) {
                    swarmsIterator.remove()
                    continue
                }

                // Schedule the actual spawning logic to run on the main server thread for safety.
                LuxAPI.getScheduler().run {
                    processSwarmSpawning(server, swarm)
                }
            }

            // If no swarms are left, cancel the task to save resources.
            if (activeSwarms.isEmpty()) {
                currentTask?.cancel()
                currentTask = null
            }
        }
    }

    /**
     * Handles the core spawning logic for a single swarm event.
     * This method is executed on the main server thread to ensure thread safety when interacting
     * with the game world.
     *
     * @param server The MinecraftServer instance.
     * @param swarm The swarm event to process.
     */
    private fun processSwarmSpawning(server: MinecraftServer, swarm: SwarmEvent) {
        val level: ServerLevel = server.getLevel(swarm.dimension) ?: return

        // Clean up any entities that have despawned or been defeated.
        swarm.activeEntities.removeIf { uuid ->
            val entity = level.getEntity(uuid)
            entity == null || !entity.isAlive
        }

        // Don't spawn more if the cap is reached.
        if (swarm.activeEntities.size >= swarm.maxActiveEntities) return

        // Calculate a random spawn position within the swarm's radius.
        val offsetX = Random.nextInt(-swarm.radius, swarm.radius)
        val offsetZ = Random.nextInt(-swarm.radius, swarm.radius)
        val targetX = swarm.centerPos.x + offsetX
        val targetZ = swarm.centerPos.z + offsetZ

        // Find the highest solid block at that position.
        val targetY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, targetX, targetZ)
        val spawnPos = BlockPos(targetX, targetY, targetZ)

        // Ensure the Pokémon doesn't spawn floating in the air.
        if (level.getBlockState(spawnPos.below()).isAir) return

        // Create the Pokémon instance.
        val species = PokemonSpecies.getByName(swarm.speciesName.lowercase()) ?: return
        val pokemon = species.create()
        pokemon.level = Random.nextInt(swarm.minLevel, swarm.maxLevel + 1)

        // Create and position the entity in the world.
        val entity = PokemonEntity(level, pokemon)
        entity.setPos(spawnPos.x.toDouble() + 0.5, spawnPos.y.toDouble(), spawnPos.z.toDouble() + 0.5)

        // Add the entity to the world and track it.
        if (level.addFreshEntity(entity)) {
            swarm.activeEntities.add(entity.uuid)
        }
    }
}