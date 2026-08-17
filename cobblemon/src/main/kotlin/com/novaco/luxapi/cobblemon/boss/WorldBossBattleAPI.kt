package com.novaco.luxapi.cobblemon.boss

import com.cobblemon.mod.common.CobblemonNetwork
import com.cobblemon.mod.common.api.battles.model.PokemonBattle
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
import java.util.Collections

/**
 * Indicates the result of a player attempting to join a boss battle.
 */
enum class BossJoinStatus {
    SUCCESS,
    ALREADY_IN_BATTLE,
    INVALID_SIDE,
    ERROR
}

/**
 * Provides the core functionality for managing multi-player vs. single boss battles (raid-style encounters).
 * This API allows developers to dynamically add players to an ongoing boss battle safely.
 */
object WorldBossBattleAPI {

    /**
     * A customizable event hook that is triggered when a player attempts to join an ongoing boss battle.
     * It should return true to allow the player to join, or false to prevent them.
     */
    var onPlayerJoinBossBattle: ((ServerPlayer, PokemonBattle, PokemonEntity) -> Boolean)? = null

    /**
     * The global default feedback handler for join attempts.
     */
    var joinFeedbackHandler: (ServerPlayer, BossJoinStatus) -> Unit = { player, status ->
        val message = when (status) {
            BossJoinStatus.SUCCESS -> "§aYou have joined the raid!"
            BossJoinStatus.ALREADY_IN_BATTLE -> "§cYou are already in this battle!"
            BossJoinStatus.INVALID_SIDE -> "§cFailed to find a valid side to join!"
            BossJoinStatus.ERROR -> "§cAn error occurred while joining the raid!"
        }
        player.sendSystemMessage(Component.literal(message))
    }

    /**
     * Registers the necessary event listeners to enable the raid join functionality.
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
     * Injects a player into an existing battle utilizing Cobblemon's native network protocol engine.
     *
     * @param player The player to be added to the battle.
     * @param battle The ongoing PokemonBattle instance.
     * @param onFeedback The callback handler for dispatching execution responses.
     */
    fun joinOngoingBattle(
        player: ServerPlayer,
        battle: PokemonBattle,
        onFeedback: ((ServerPlayer, BossJoinStatus) -> Unit) = joinFeedbackHandler
    ) {
        if (battle.actors.any { it.isForPlayer(player) }) {
            onFeedback.invoke(player, BossJoinStatus.ALREADY_IN_BATTLE)
            return
        }

        val playerSide = battle.side1 // Default to side 1 as the players' raiding faction

        if (playerSide != null) {
            try {
                val activeBattleInstance = BattleRegistry.getBattle(battle.battleId)
                if (activeBattleInstance == null) {
                    onFeedback.invoke(player, BossJoinStatus.ERROR)
                    return
                }

                val battlePokemonList = player.party().map { BattlePokemon(it) }
                val newActor = PlayerBattleActor(player.uuid, battlePokemonList)
                newActor.battle = activeBattleInstance

                // Utilizing Cobblemon's global network synchronizer infrastructure to initialize battle interface
                val initPacket = BattleInitializePacket(activeBattleInstance, playerSide)
                CobblemonNetwork.sendPacketToPlayers(Collections.singletonList(player), initPacket)

                onFeedback.invoke(player, BossJoinStatus.SUCCESS)
            } catch (e: IllegalStateException) {
                e.printStackTrace()
                onFeedback.invoke(player, BossJoinStatus.ERROR)
            } catch (e: IllegalArgumentException) {
                e.printStackTrace()
                onFeedback.invoke(player, BossJoinStatus.ERROR)
            } catch (e: NullPointerException) {
                e.printStackTrace()
                onFeedback.invoke(player, BossJoinStatus.ERROR)
            } catch (e: ConcurrentModificationException) {
                e.printStackTrace()
                onFeedback.invoke(player, BossJoinStatus.ERROR)
            }
        } else {
            onFeedback.invoke(player, BossJoinStatus.INVALID_SIDE)
        }
    }
}