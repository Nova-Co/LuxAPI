package com.novaco.luxapi.cobblemon.pasture

import com.cobblemon.mod.common.block.entity.PokemonPastureBlockEntity
import com.cobblemon.mod.common.pokemon.Pokemon
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import java.util.UUID

/**
 * Wraps Cobblemon's real pasture/daycare system. Cobblemon has no global pasture registry of
 * its own — state lives directly on the [PokemonPastureBlockEntity] at a given [BlockPos] — so
 * this wrapper doesn't invent one either. Devs resolve a pasture from a position they already
 * know (e.g. one their own placement logic tracked), exactly like Cobblemon's own block
 * interaction code does.
 */
object PastureManager {

    /**
     * Resolves the pasture block entity at [pos] in [world], or `null` if there isn't one there.
     */
    fun getPastureAt(world: ServerLevel, pos: BlockPos): PokemonPastureBlockEntity? {
        return world.getBlockEntity(pos) as? PokemonPastureBlockEntity
    }

    /**
     * Tethers [pokemon] into [pasture] on [player]'s behalf, spawning its roaming entity behind
     * the pasture block in [directionToBehind]. Returns `false` if there's no room to place the
     * entity, the pasture/per-player limits are already full, or the Pokémon is fainted — same
     * checks Cobblemon's own pasture GUI performs.
     */
    fun tetherPokemon(pasture: PokemonPastureBlockEntity, player: ServerPlayer, pokemon: Pokemon, directionToBehind: Direction): Boolean {
        return pasture.tether(player, pokemon, directionToBehind)
    }

    /**
     * Releases the Pokémon identified by [pokemonId] from [pasture], returning it to normal PC
     * storage. Returns `false` if that Pokémon wasn't tethered there.
     */
    fun releasePokemon(pasture: PokemonPastureBlockEntity, pokemonId: UUID): Boolean {
        val wasTethered = pasture.tetheredPokemon.any { it.pokemonId == pokemonId }
        pasture.releasePokemon(pokemonId)
        return wasTethered
    }

    /**
     * Releases every Pokémon [playerId] has tethered in [pasture]. Returns the UUIDs of the
     * Pokémon that were released (empty if [playerId] had none tethered there).
     */
    fun releaseAllPokemon(pasture: PokemonPastureBlockEntity, playerId: UUID): List<UUID> {
        return pasture.releaseAllPokemon(playerId)
    }

    /**
     * All Pokémon currently tethered in [pasture], across every player.
     */
    fun getTetheredPokemon(pasture: PokemonPastureBlockEntity): List<PastureTethering> {
        return pasture.tetheredPokemon.map {
            PastureTethering(
                pokemonId = it.pokemonId,
                ownerId = it.playerId,
                ownerName = it.playerName,
                pcId = it.pcId,
                entityId = it.entityId
            )
        }
    }

    /**
     * Pokémon [playerId] specifically has tethered in [pasture].
     */
    fun getTetheredPokemon(pasture: PokemonPastureBlockEntity, playerId: UUID): List<PastureTethering> {
        return getTetheredPokemon(pasture).filter { it.ownerId == playerId }
    }
}
