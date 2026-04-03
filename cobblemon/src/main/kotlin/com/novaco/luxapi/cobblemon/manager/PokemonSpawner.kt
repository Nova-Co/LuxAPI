package com.novaco.luxapi.cobblemon.manager

import com.cobblemon.mod.common.api.pokemon.PokemonProperties
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.CobblemonEntities // 🌟 Import เพิ่มเติมสำหรับเสกร่าง
import com.novaco.luxapi.commons.player.LuxPlayer
import net.minecraft.server.level.ServerPlayer

/**
 * Utility object dedicated to spawning Pokémon entities into the Minecraft world.
 * This utilizes Cobblemon's internal PokemonProperties parser to quickly generate
 * custom Pokémon with specific attributes.
 *
 */
object PokemonSpawner {

    /**
     * Spawns a wild Pokémon entity directly in front of the specified player.
     * It dynamically calculates the player's looking direction to place the entity
     * a few blocks ahead.
     *
     * @param player The [LuxPlayer] targeting the spawn location.
     * @param species The name of the Pokémon species (e.g., "charizard").
     * @param level The starting level of the Pokémon (default is 1).
     * @param isShiny Whether the Pokémon should be spawned as a shiny variant.
     * @param form An optional form string (e.g., "alola", "gmax"). Null if default.
     * @return The spawned [PokemonEntity] instance, or null if spawning fails.
     */
    fun spawnInFrontOf(
        player: LuxPlayer,
        species: String,
        level: Int = 1,
        isShiny: Boolean = false,
        form: String? = null
    ): PokemonEntity? {
        val serverPlayer = player.parent as ServerPlayer
        val serverLevel = serverPlayer.serverLevel()

        val lookVector = serverPlayer.lookAngle
        val spawnPos = serverPlayer.position().add(lookVector.scale(2.0))

        val propertiesBuilder = StringBuilder(species)
        propertiesBuilder.append(" level=$level")

        if (isShiny) {
            propertiesBuilder.append(" shiny=yes")
        }

        if (form != null && form.isNotBlank()) {
            propertiesBuilder.append(" form=$form")
        }

        val pokemon = PokemonProperties.parse(propertiesBuilder.toString()).create()

        val pokemonEntity = CobblemonEntities.POKEMON.create(serverLevel) ?: return null
        pokemonEntity.pokemon = pokemon
        pokemonEntity.setPos(spawnPos.x, spawnPos.y, spawnPos.z)

        val success = serverLevel.addFreshEntity(pokemonEntity)

        return if (success) pokemonEntity else null
    }

    /**
     * Spawns a Pokémon using an explicit Cobblemon property string.
     * Useful for advanced developers who want to use raw spec strings.
     *
     * @param player The target [LuxPlayer].
     * @param specString The raw Cobblemon property string (e.g., "pikachu lvl=50 hiddenability").
     * @return The spawned [PokemonEntity], or null.
     */
    fun spawnFromSpec(player: LuxPlayer, specString: String): PokemonEntity? {
        val serverPlayer = player.parent as ServerPlayer
        val serverLevel = serverPlayer.serverLevel()
        val lookVector = serverPlayer.lookAngle
        val spawnPos = serverPlayer.position().add(lookVector.scale(2.0))

        val pokemon = PokemonProperties.parse(specString).create()

        val pokemonEntity = CobblemonEntities.POKEMON.create(serverLevel) ?: return null
        pokemonEntity.pokemon = pokemon
        pokemonEntity.setPos(spawnPos.x, spawnPos.y, spawnPos.z)

        return if (serverLevel.addFreshEntity(pokemonEntity)) pokemonEntity else null
    }
}