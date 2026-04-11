package com.novaco.luxapi.cobblemon.pokemon

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.novaco.luxapi.commons.player.LuxPlayer
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.phys.Vec3

/**
 * Spawns a custom wild Pokémon safely in front of the player.
 *
 * @param speciesName The name of the Pokémon species (e.g., "charizard").
 * @param distance Blocks away from the player to spawn the Pokémon.
 * @param builder The configuration block for the Pokémon.
 * @return The spawned [PokemonEntity], or null if generation failed.
 */
fun LuxPlayer.spawnWildPokemon(
    speciesName: String,
    distance: Double = 3.0,
    builder: WildPokemonBuilder.() -> Unit
): PokemonEntity? {
    val serverPlayer = this.parent as ServerPlayer
    val serverLevel = serverPlayer.serverLevel()

    // Calculate position exactly in front of the player (flattened Y-axis to prevent burying)
    val flatLookVector = Vec3.directionFromRotation(0.0f, serverPlayer.yRot)
    val spawnPos = serverPlayer.position().add(flatLookVector.scale(distance))

    val wildBuilder = WildPokemonBuilder(speciesName)
    wildBuilder.builder()

    return wildBuilder.spawn(serverLevel, spawnPos)
}