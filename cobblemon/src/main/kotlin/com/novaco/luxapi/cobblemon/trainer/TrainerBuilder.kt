package com.novaco.luxapi.cobblemon.trainer

import com.cobblemon.mod.common.CobblemonEntities
import com.cobblemon.mod.common.api.npc.NPCClasses
import com.cobblemon.mod.common.api.pokemon.PokemonProperties
import com.cobblemon.mod.common.api.storage.party.NPCPartyStore
import com.cobblemon.mod.common.entity.npc.NPCEntity
import com.cobblemon.mod.common.pokemon.Pokemon
import com.novaco.luxapi.commons.player.LuxPlayer
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer

/**
 * A fluent builder pattern designed to construct and spawn custom NPC Trainers dynamically.
 * Developers can define the NPC's display name, dialogue, and complete battle party
 * entirely through code, eliminating the need for static configuration files.
 */
class TrainerBuilder(private val spawner: LuxPlayer) {

    private var trainerName: Component = Component.literal("Pokémon Trainer")
    private val trainerParty: MutableList<Pokemon> = mutableListOf()
    private var interactDialog: Component = Component.literal("Let's battle!")

    private var skinUsername: String = "Ash"
    private var lookAtPlayer: Boolean = true

    /**
     * Sets the display name floating above the NPC's head.
     *
     * @param name The desired name (supports formatting codes).
     * @return The current builder instance.
     */
    fun setName(name: String): TrainerBuilder {
        this.trainerName = Component.literal(name)
        return this
    }

    /**
     * Directly adds a pre-configured [Pokemon] object to the NPC's battle party.
     * The party has a strict maximum of 6 Pokémon.
     *
     * @param pokemon The instantiated Pokémon.
     * @return The current builder instance.
     */
    fun addPokemon(pokemon: Pokemon): TrainerBuilder {
        if (trainerParty.size < 6) {
            trainerParty.add(pokemon)
        }
        return this
    }

    /**
     * Parses a Cobblemon specification string and adds the resulting Pokémon
     * to the NPC's battle party.
     *
     * @param spec The property string (e.g., "charizard lvl=50 shiny=yes").
     * @return The current builder instance.
     */
    fun addPokemon(spec: String): TrainerBuilder {
        if (trainerParty.size < 6) {
            val pokemon = PokemonProperties.parse(spec).create()
            trainerParty.add(pokemon)
        }
        return this
    }

    /**
     * Sets the dialogue text the NPC will display upon interaction or before a battle.
     *
     * @param dialog The spoken text.
     * @return The current builder instance.
     */
    fun setDialog(dialog: String): TrainerBuilder {
        this.interactDialog = Component.literal(dialog)
        return this
    }

    /**
     *
     */
    fun setSkin(username: String): TrainerBuilder {
        this.skinUsername = username
        return this
    }

    fun setLook(look: Boolean): TrainerBuilder {
        this.lookAtPlayer = look
        return this
    }

    /**
     * Finalizes the builder configuration and spawns the NPC Entity into the world,
     * placing it exactly two blocks in front of the initiating player.
     *
     * @return The spawned [NPCEntity], or null if the spawn action failed.
     */
    fun spawn(): NPCEntity? {
        val serverPlayer = spawner.parent as ServerPlayer
        val serverLevel = serverPlayer.serverLevel()
        val lookVector = serverPlayer.lookAngle
        val spawnPos = serverPlayer.position().add(lookVector.scale(2.0))
        val npcEntity = CobblemonEntities.NPC.create(serverLevel) ?: return null

        val availableClasses = NPCClasses.classes

        val classNames = availableClasses.joinToString(", ") { it.id.toString() }
        println("[LuxAPI] Available NPC Classes: $classNames")

        val targetNames = listOf("standard", "player", "trainer", "npc", "human")
        var validClass = availableClasses.firstOrNull { classObj ->
            targetNames.any { classObj.id.path.contains(it) }
        }

        if (validClass == null) {
            validClass = availableClasses.firstOrNull { it.id.path != "dummy" }
        }

        if (validClass != null) {
            npcEntity.npc = validClass
        } else {
            println("[LuxAPI] CRITICAL: No valid NPC classes found!")
        }

        npcEntity.customName = trainerName
        npcEntity.isCustomNameVisible = true
        npcEntity.setPos(spawnPos.x, spawnPos.y, spawnPos.z)

        npcEntity.loadTextureFromGameProfileName(skinUsername)

        if (npcEntity.party == null) {
            npcEntity.party = NPCPartyStore(npcEntity)
        }

        npcEntity.party?.let { partyStore ->
            for (pokemon in trainerParty) {
                partyStore.add(pokemon)
            }
        }

        val success = serverLevel.addFreshEntity(npcEntity)

        if (success && lookAtPlayer) {
            npcEntity.addTag("lux_look_at_interactor")
        }

        return if (success) npcEntity else null
    }
}