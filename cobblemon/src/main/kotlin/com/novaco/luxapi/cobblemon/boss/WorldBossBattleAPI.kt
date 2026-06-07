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
     * The global default feedback handler for join attempts.
     * Developers can override this entirely to integrate their own language files or display methods (e.g., Titles/ActionBars).
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
    fun joinOngoingBattle(
        player: ServerPlayer,
        battle: PokemonBattle,
        onFeedback: ((ServerPlayer, BossJoinStatus) -> Unit) = joinFeedbackHandler
    ) {
        // Prevent a player from joining the same battle multiple times
        if (battle.actors.any { it.isForPlayer(player) }) {
            onFeedback.invoke(player, BossJoinStatus.ALREADY_IN_BATTLE)
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
                onFeedback.invoke(player, BossJoinStatus.ERROR)
                return
            }

            newActor.battle = battle

            // Send the initialization packet to the new player to sync their UI
            val initPacket = BattleInitializePacket(battle, playerSide)
            newActor.sendUpdate(initPacket)

            onFeedback.invoke(player, BossJoinStatus.SUCCESS)
        } else {
            onFeedback.invoke(player, BossJoinStatus.INVALID_SIDE)
        }
    }
}