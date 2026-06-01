package com.novaco.luxapi.cobblemon.boss

import com.cobblemon.mod.common.api.battles.model.PokemonBattle
import com.cobblemon.mod.common.api.battles.model.actor.BattleActor
import com.cobblemon.mod.common.api.events.CobblemonEvents
import com.cobblemon.mod.common.battles.BattleRegistry
import com.cobblemon.mod.common.battles.BattleSide
import com.cobblemon.mod.common.battles.actor.PlayerBattleActor
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.net.messages.client.battle.BattleInitializePacket
import com.cobblemon.mod.common.util.party
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer

/**
 * Provides the core functionality for managing multi-player vs. single boss battles (raid-style encounters).
 * This API allows developers to dynamically add players to an ongoing boss battle.
 */
object WorldBossBattleAPI {

    /**
     * A customizable event hook that is triggered when a player attempts to join an ongoing boss battle.
     * Developers can implement this to add custom logic, such as checking for raid capacity or player eligibility.
     * It should return `true` to allow the player to join, or `false` to prevent them.
     */
    var onPlayerJoinBossBattle: ((ServerPlayer, PokemonBattle, PokemonEntity) -> Boolean)? = null

    /**
     * Registers the necessary event listeners to enable the raid join functionality.
     * It specifically listens for a Poke Ball being thrown at a boss, interpreting it as a request to join the battle.
     */
    fun register() {
        CobblemonEvents.THROWN_POKEBALL_HIT.subscribe { event ->
            val targetBoss = event.pokemon
            val player = event.pokeBall.owner as? ServerPlayer ?: return@subscribe

            // Check if the target is a designated world boss
            if (targetBoss.tags.contains("lux_is_world_boss") || targetBoss.tags.contains("lux_is_boss")) {
                // If the boss is already in a battle, treat this as a join attempt
                if (targetBoss.isBattling && targetBoss.battleId != null) {
                    val activeBattle = BattleRegistry.getBattle(targetBoss.battleId!!) ?: return@subscribe

                    event.cancel() // Prevent the Poke Ball from being used

                    // Check with the custom hook if the player is allowed to join
                    val shouldJoin = onPlayerJoinBossBattle?.invoke(player, activeBattle, targetBoss) ?: true
                    if (shouldJoin) {
                        joinOngoingBattle(player, activeBattle)
                    }
                }
            }
        }
    }

    /**
     * Injects a player into an existing battle.
     * This method handles the logic of adding a new player actor to the correct side of the battle
     * and synchronizing the battle state with the new player's client.
     *
     * @param player The player to be added to the battle.
     * @param battle The ongoing `PokemonBattle` instance.
     */
    fun joinOngoingBattle(player: ServerPlayer, battle: PokemonBattle) {
        // Prevent a player from joining the same battle multiple times
        if (battle.actors.any { it.isForPlayer(player) }) {
            player.sendSystemMessage(Component.literal("§cYou are already in this battle!"))
            return
        }

        // Find the side that is opposing the boss (the "player" side)
        val playerSide = battle.actors.firstOrNull { actor ->
            actor.pokemonList.none { pkmn ->
                pkmn.entity?.tags?.contains("lux_is_boss") == true || pkmn.entity?.tags?.contains("lux_is_world_boss") == true
            }
        }?.getSide()

        if (playerSide != null) {
            val battlePokemonList = player.party().map { BattlePokemon(it) }
            val newActor = PlayerBattleActor(player.uuid, battlePokemonList)

            // Use reflection to add the new actor to the battle, as the underlying collections are immutable
            try {
                val actorsField = BattleSide::class.java.getDeclaredField("actors")
                actorsField.isAccessible = true

                @Suppress("UNCHECKED_CAST")
                val currentActors = actorsField.get(playerSide) as Array<BattleActor>
                val newActorsArray = currentActors.plus(newActor)

                actorsField.set(playerSide, newActorsArray)

                // Also add to the battle's main actor list for proper tracking
                @Suppress("UNCHECKED_CAST")
                val battleActorsCollection = battle.actors as? MutableCollection<BattleActor>
                battleActorsCollection?.add(newActor)

            } catch (e: Exception) {
                e.printStackTrace()
                player.sendSystemMessage(Component.literal("§cAn error occurred while joining the raid!"))
                return
            }

            newActor.battle = battle

            // Send the initialization packet to the new player to sync their UI
            val initPacket = BattleInitializePacket(battle, playerSide)
            newActor.sendUpdate(initPacket)

            player.sendSystemMessage(Component.literal("§aYou have joined the raid!"))
        } else {
            player.sendSystemMessage(Component.literal("§cFailed to find a valid side to join!"))
        }
    }
}