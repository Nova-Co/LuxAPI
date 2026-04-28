package com.novaco.luxapi.cobblemon.npc

import com.cobblemon.mod.common.CobblemonEntities
import com.cobblemon.mod.common.api.npc.NPCClasses
import com.cobblemon.mod.common.api.npc.configuration.NPCInteractConfiguration
import com.cobblemon.mod.common.api.pokemon.PokemonProperties
import com.cobblemon.mod.common.api.storage.party.NPCPartyStore
import com.cobblemon.mod.common.entity.npc.NPCEntity
import com.cobblemon.mod.common.pokemon.Pokemon
import com.novaco.luxapi.commons.player.LuxPlayer
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.phys.Vec3
import java.util.concurrent.CompletableFuture

/**
 * A highly fluent builder for creating versatile NPCs in Cobblemon.
 * Supports static NPCs, interactive storytellers, and battle trainers.
 */
class LuxNPCBuilder(private val spawner: LuxPlayer) {

    private var npcName: Component = Component.literal("Villager")
    private var skinUsername: String? = null
    private var lookAtPlayer: Boolean = true
    private var isMovable: Boolean = false

    private val trainerParty: MutableList<Pokemon> = mutableListOf()
    private var interactionHandler: ((ServerPlayer, NPCEntity) -> Unit)? = null

    /** Sets the floating display name above the NPC. */
    fun name(name: String): LuxNPCBuilder {
        this.npcName = Component.literal(name)
        return this
    }

    /** Sets the skin of the NPC based on a Minecraft player's username. */
    fun skin(username: String): LuxNPCBuilder {
        this.skinUsername = username
        return this
    }

    /** Should the NPC lock its head rotation to look at nearby players? */
    fun lookAtPlayer(look: Boolean): LuxNPCBuilder {
        this.lookAtPlayer = look
        return this
    }

    /** Should the NPC be able to wander around? */
    fun movable(movable: Boolean): LuxNPCBuilder {
        this.isMovable = movable
        return this
    }

    /** Adds a specific Pokemon to the NPC's battle party (max 6). */
    fun addPokemon(pokemon: Pokemon): LuxNPCBuilder {
        if (trainerParty.size < 6) trainerParty.add(pokemon)
        return this
    }

    /** Adds a Pokemon using the Cobblemon Spec string format (e.g., "charizard lvl=50"). */
    fun addPokemon(spec: String): LuxNPCBuilder {
        if (trainerParty.size < 6) {
            trainerParty.add(PokemonProperties.parse(spec).create())
        }
        return this
    }

    /**
     * Defines the code to execute when a player right-clicks the NPC.
     * Perfect for triggering LuxDialogueBuilder or opening a Shop GUI.
     */
    fun onInteract(action: (player: ServerPlayer, npc: NPCEntity) -> Unit): LuxNPCBuilder {
        this.interactionHandler = action
        return this
    }

    /**
     * Spawns the NPC 2 blocks in front of the initiating player.
     * @return The spawned NPCEntity or null.
     */
    fun spawn(): NPCEntity? {
        val serverPlayer = spawner.parent as ServerPlayer
        val serverLevel = serverPlayer.serverLevel()

        val flatLookVector = Vec3.directionFromRotation(0.0f, serverPlayer.yRot)
        val spawnPos = serverPlayer.position().add(flatLookVector.scale(2.0))

        val npcEntity = CobblemonEntities.NPC.create(serverLevel) ?: return null

        // Assign default class and name
        NPCClasses.getByName("standard")?.let { npcEntity.npc = it }
        npcEntity.customName = npcName
        npcEntity.isCustomNameVisible = true
        npcEntity.isMovable = isMovable

        // Setup position and rotation
        npcEntity.setPos(spawnPos.x, spawnPos.y, spawnPos.z)
        npcEntity.yRot = serverPlayer.yRot + 180.0f
        npcEntity.yHeadRot = npcEntity.yRot

        // Inject Party if provided
        if (trainerParty.isNotEmpty()) {
            if (npcEntity.party == null) {
                npcEntity.party = NPCPartyStore(npcEntity)
            }
            npcEntity.party?.let { partyStore ->
                trainerParty.forEach { partyStore.add(it) }
            }
        }

        // Inject Custom Interaction
        if (interactionHandler != null) {
            npcEntity.interaction = LuxCustomInteraction(interactionHandler!!)
        }

        // Spawn and Apply Async Textures
        val success = serverLevel.addFreshEntity(npcEntity)
        if (success) {
            if (lookAtPlayer) {
                npcEntity.addTag("lux_look_at_interactor")
            }
            skinUsername?.let { username ->
                CompletableFuture.runAsync {
                    try {
                        npcEntity.loadTextureFromGameProfileName(username)
                    } catch (e: Exception) {
                        println("[LuxAPI] Failed to load skin for $username: ${e.message}")
                    }
                }
            }
        }

        return if (success) npcEntity else null
    }

    /**
     * Internal class to bridge Kotlin lambdas with Cobblemon's NPCInteractConfiguration.
     * We don't need to register this to the global registry because it's purely server-side logic.
     */
    private class LuxCustomInteraction(
        private val action: (ServerPlayer, NPCEntity) -> Unit
    ) : NPCInteractConfiguration {
        override val type: String = "lux_custom"
        override fun interact(npc: NPCEntity, player: ServerPlayer): Boolean {
            action(player, npc)
            return true
        }
        override fun encode(buffer: RegistryFriendlyByteBuf) {}
        override fun decode(buffer: RegistryFriendlyByteBuf) {}
        override fun writeToNBT(compoundTag: CompoundTag) {}
        override fun readFromNBT(compoundTag: CompoundTag) {}
        override fun isDifferentTo(other: NPCInteractConfiguration) = true
    }
}