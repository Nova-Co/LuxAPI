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
 * Core API for managing Multi-player vs 1 Boss Battles (Raid style).
 * Gives developers the tools to add players to an ongoing boss battle.
 */
object WorldBossBattleAPI {

    /**
     * Event Hook: Triggered when a player tries to join an ongoing Boss Battle.
     * Devs can set this to handle specific logic (e.g., checking if the raid is full).
     */
    var onPlayerJoinBossBattle: ((ServerPlayer, PokemonBattle, PokemonEntity) -> Boolean)? = null

    /**
     * Registers the listener to intercept Poké Ball throws and handle raid joins.
     */
    fun register() {
        CobblemonEvents.THROWN_POKEBALL_HIT.subscribe { event ->
            val targetBoss = event.pokemon
            val player = event.pokeBall.owner as? ServerPlayer ?: return@subscribe

            if (targetBoss.tags.contains("lux_is_world_boss") || targetBoss.tags.contains("lux_is_boss")) {
                if (targetBoss.isBattling && targetBoss.battleId != null) {
                    val activeBattle = BattleRegistry.getBattle(targetBoss.battleId!!) ?: return@subscribe

                    event.cancel()

                    val shouldJoin = onPlayerJoinBossBattle?.invoke(player, activeBattle, targetBoss) ?: true
                    if (shouldJoin) {
                        joinOngoingBattle(player, activeBattle)
                    }
                }
            }
        }
    }

    /**
     * Injects a new player into an existing Turn-Based Battle on the "Player" side.
     * * @param player The player joining the raid.
     * @param battle The ongoing PokemonBattle instance.
     */
    fun joinOngoingBattle(player: ServerPlayer, battle: PokemonBattle) {
        if (battle.actors.any { it.isForPlayer(player) }) {
            player.sendSystemMessage(Component.literal("§cYou are already in this battle!"))
            return
        }

        val playerSide = battle.actors.firstOrNull { actor ->
            actor.pokemonList.none { pkmn ->
                pkmn.entity?.tags?.contains("lux_is_boss") == true || pkmn.entity?.tags?.contains("lux_is_world_boss") == true
            }
        }?.getSide()

        if (playerSide != null) {
            val battlePokemonList = player.party().map { BattlePokemon(it) }
            val newActor = PlayerBattleActor(player.uuid, battlePokemonList)

            // Reflection hack to inject the new actor into the immutable BattleSide array
            try {
                val actorsField = BattleSide::class.java.getDeclaredField("actors")
                actorsField.isAccessible = true

                @Suppress("UNCHECKED_CAST")
                val currentActors = actorsField.get(playerSide) as Array<BattleActor>
                val newActorsArray = currentActors.plus(newActor)

                actorsField.set(playerSide, newActorsArray)

                @Suppress("UNCHECKED_CAST")
                val battleActorsCollection = battle.actors as? MutableCollection<BattleActor>
                battleActorsCollection?.add(newActor)

            } catch (e: Exception) {
                e.printStackTrace()
                player.sendSystemMessage(Component.literal("§cAn error occurred while joining the raid!"))
                return
            }

            newActor.battle = battle

            // Force UI sync using BattleInitializePacket with the designated ally side
            val initPacket = BattleInitializePacket(battle, playerSide)
            newActor.sendUpdate(initPacket)

            player.sendSystemMessage(Component.literal("§aYou have joined the raid!"))
        } else {
            player.sendSystemMessage(Component.literal("§cFailed to find a valid side to join!"))
        }
    }
}