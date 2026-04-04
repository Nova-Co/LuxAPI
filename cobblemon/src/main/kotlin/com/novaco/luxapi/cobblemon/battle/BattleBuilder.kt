package com.novaco.luxapi.cobblemon.battle

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.battles.model.PokemonBattle
import com.cobblemon.mod.common.battles.BattleFormat
import com.cobblemon.mod.common.battles.BattleSide
import com.cobblemon.mod.common.battles.actor.PlayerBattleActor
import com.cobblemon.mod.common.battles.actor.PokemonBattleActor
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.util.party
import com.novaco.luxapi.commons.player.LuxPlayer
import net.minecraft.server.level.ServerPlayer

/**
 * A fluent builder pattern designed to configure and initiate Pokemon battles effortlessly.
 * Abstracts the complexities of the internal BattleRegistry for quick configuration.
 */
class BattleBuilder(private val initiator: LuxPlayer) {

    private var isDoubleBattle: Boolean = false
    private var allowSpectators: Boolean = true

    /**
     * Sets the battle format to Doubles (2v2) instead of the standard Singles (1v1).
     *
     * @param isDouble True for a double battle format.
     * @return The current builder instance for chaining.
     */
    fun setDoubleBattle(isDouble: Boolean): BattleBuilder {
        this.isDoubleBattle = isDouble
        return this
    }

    /**
     * Determines if other players are allowed to spectate this match.
     *
     * @param allow True to allow spectators, false to make it a private match.
     * @return The current builder instance for chaining.
     */
    fun setSpectatorAllowed(allow: Boolean): BattleBuilder {
        this.allowSpectators = allow
        return this
    }

    /**
     * Extracts conscious Pokemon from a player's party and converts them
     * into BattlePokemon instances.
     */
    private fun getBattleReadyParty(player: ServerPlayer): List<BattlePokemon> {
        val party = player.party()
        val battleReadyList = mutableListOf<BattlePokemon>()

        for (pokemon in party) {
            if (pokemon != null && pokemon.currentHealth > 0) {
                battleReadyList.add(BattlePokemon(pokemon))
            }
        }
        return battleReadyList
    }

    /**
     * Executes the battle initialization against a wild Pokemon entity in the world.
     *
     * @param wildEntity The target wild Pokemon entity.
     * @return The active PokemonBattle instance, or null if the battle could not start.
     */
    fun startAgainstWild(wildEntity: PokemonEntity): PokemonBattle? {
        val serverPlayer = initiator.parent as ServerPlayer
        val battleParty = getBattleReadyParty(serverPlayer)

        if (battleParty.isEmpty()) return null

        val format = if (isDoubleBattle) BattleFormat.GEN_9_DOUBLES else BattleFormat.GEN_9_SINGLES
        val p1Side = BattleSide(PlayerBattleActor(serverPlayer.uuid, battleParty))
        val wildBattlePokemon = BattlePokemon(wildEntity.pokemon)
        val p2Side = BattleSide(PokemonBattleActor(wildEntity.uuid, wildBattlePokemon, -1F))

        var activeBattle: PokemonBattle? = null

        Cobblemon.battleRegistry.startBattle(format, p1Side, p2Side).ifSuccessful { battle ->
            activeBattle = battle
            // Note: Spectator settings can be injected into the activeBattle here in the future.
        }

        return activeBattle
    }

    /**
     * Executes a Player vs Player (PvP) battle initialization.
     *
     * @param opponent The targeted player to battle against.
     * @return The active PokemonBattle instance, or null if the battle could not start.
     */
    fun startAgainstPlayer(opponent: LuxPlayer): PokemonBattle? {
        val p1 = initiator.parent as ServerPlayer
        val p2 = opponent.parent as ServerPlayer

        val p1Party = getBattleReadyParty(p1)
        val p2Party = getBattleReadyParty(p2)

        if (p1Party.isEmpty() || p2Party.isEmpty()) return null

        val format = if (isDoubleBattle) BattleFormat.GEN_9_DOUBLES else BattleFormat.GEN_9_SINGLES

        val p1Side = BattleSide(PlayerBattleActor(p1.uuid, p1Party))
        val p2Side = BattleSide(PlayerBattleActor(p2.uuid, p2Party))

        var activeBattle: PokemonBattle? = null

        Cobblemon.battleRegistry.startBattle(format, p1Side, p2Side).ifSuccessful { battle ->
            activeBattle = battle
        }

        return activeBattle
    }
}