package com.novaco.luxapi.cobblemon.battle

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.battles.model.PokemonBattle
import com.cobblemon.mod.common.battles.BattleFormat
import com.cobblemon.mod.common.battles.BattleRegistry
import com.cobblemon.mod.common.battles.BattleSide
import com.cobblemon.mod.common.battles.actor.PlayerBattleActor
import com.cobblemon.mod.common.battles.actor.PokemonBattleActor
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon
import com.cobblemon.mod.common.entity.npc.NPCEntity
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.util.party
import com.novaco.luxapi.commons.player.LuxPlayer
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer

/**
 * A fluent builder pattern designed to configure and initiate Pokemon battles effortlessly.
 * Abstracts the complexities of the internal BattleRegistry for quick configuration.
 */
class BattleBuilder(private val initiator: LuxPlayer) {

    private var isDoubleBattle: Boolean = false
    private var allowSpectators: Boolean = true

    fun setDoubleBattle(isDouble: Boolean): BattleBuilder {
        this.isDoubleBattle = isDouble
        return this
    }

    fun setSpectatorAllowed(allow: Boolean): BattleBuilder {
        this.allowSpectators = allow
        return this
    }

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
        }

        return activeBattle
    }

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

    /**
     * Executes a battle initialization against a custom NPC Entity.
     * Uses Cobblemon's native Player VS NPC (PVN) builder for perfect state management.
     */
    fun startAgainstNPC(npcEntity: NPCEntity): PokemonBattle? {
        val serverPlayer = initiator.parent as ServerPlayer

        if (npcEntity.isInBattle() || BattleRegistry.getBattleByParticipatingPlayer(serverPlayer) != null) {
            return null
        }

        val p1Party = getBattleReadyParty(serverPlayer)

        if (p1Party.isEmpty()) {
            serverPlayer.sendSystemMessage(Component.literal("§cYou don't have any conscious Pokemon to battle with!"))
            return null
        }

        val format = if (isDoubleBattle) BattleFormat.GEN_9_DOUBLES else BattleFormat.GEN_9_SINGLES
        var activeBattle: PokemonBattle? = null

        com.cobblemon.mod.common.battles.BattleBuilder.pvn(
            player = serverPlayer,
            npcEntity = npcEntity,
            battleFormat = format,
            cloneParties = false,
            healFirst = false
        ).ifSuccessful { battle ->
            activeBattle = battle
        }.ifErrored { error ->
            val errorMsg = error.errors.joinToString(", ") { it.javaClass.simpleName }
            serverPlayer.sendSystemMessage(Component.literal("§cFailed to start battle: $errorMsg"))
        }

        return activeBattle
    }
}