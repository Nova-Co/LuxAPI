package com.novaco.luxapi.cobblemon.pokemon

import com.cobblemon.mod.common.CobblemonEntities
import com.cobblemon.mod.common.api.pokemon.PokemonSpecies
import com.cobblemon.mod.common.api.pokemon.stats.Stats
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.phys.Vec3

/**
 * A powerful builder for creating and spawning customized Wild Pokémon.
 * Highly useful for generating Boss Pokémon, Quest Targets, or Event Spawns
 * with precise control over stats, appearance, and entity tags.
 */
class WildPokemonBuilder(private val speciesName: String) {

    private var level: Int = 50
    private var shiny: Boolean? = null
    private var customName: String? = null
    private var makeFlawless: Boolean = false
    private val tags = mutableListOf<String>()

    /**
     * Sets the level of the spawned Pokémon.
     */
    fun setLevel(level: Int) = apply { this.level = level }

    /**
     * Forces the Pokémon to be shiny (or explicitly not shiny).
     */
    fun setShiny(isShiny: Boolean = true) = apply { this.shiny = isShiny }

    /**
     * Gives the Pokémon a custom display name (e.g., "§c§lThe Volcano Lord").
     */
    fun setCustomName(name: String) = apply { this.customName = name }

    /**
     * Automatically sets all IVs (Individual Values) to their maximum (31).
     * Perfect for Boss Pokémon.
     */
    fun setFlawless() = apply { this.makeFlawless = true }

    /**
     * Adds a specific Minecraft Entity Tag.
     * Highly recommended for tracking Boss deaths or applying custom drops.
     */
    fun addTag(tag: String) = apply { this.tags.add(tag) }

    /**
     * Finalizes the configuration and spawns the Pokémon into the world.
     *
     * @param serverLevel The world/dimension to spawn the Pokémon in.
     * @param spawnPos The exact XYZ coordinates for the spawn.
     * @return The spawned [PokemonEntity], or null if the species is invalid.
     */
    fun spawn(serverLevel: ServerLevel, spawnPos: Vec3): PokemonEntity? {
        // Resolve Species
        val species = PokemonSpecies.getByName(speciesName.lowercase()) ?: return null

        // Create Base Pokemon Data
        val pokemon = species.create(level)

        shiny?.let { pokemon.shiny = it }

        if (makeFlawless) {
            pokemon.ivs.set(Stats.HP, 31)
            pokemon.ivs.set(Stats.ATTACK, 31)
            pokemon.ivs.set(Stats.DEFENCE, 31)
            pokemon.ivs.set(Stats.SPECIAL_ATTACK, 31)
            pokemon.ivs.set(Stats.SPECIAL_DEFENCE, 31)
            pokemon.ivs.set(Stats.SPEED, 31)
        }

        // Create the Physical Entity
        val pokemonEntity = CobblemonEntities.POKEMON.create(serverLevel) ?: return null
        pokemonEntity.pokemon = pokemon
        pokemonEntity.setPos(spawnPos.x, spawnPos.y, spawnPos.z)

        // Apply Visuals and Tags
        if (customName != null) {
            pokemonEntity.customName = Component.literal(customName)
            pokemonEntity.isCustomNameVisible = true
        }

        tags.forEach { pokemonEntity.addTag(it) }

        // Spawn into the World
        serverLevel.addFreshEntity(pokemonEntity)

        return pokemonEntity
    }
}