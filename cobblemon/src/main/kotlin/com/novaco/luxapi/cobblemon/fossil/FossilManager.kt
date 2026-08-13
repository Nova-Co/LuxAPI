package com.novaco.luxapi.cobblemon.fossil

import com.cobblemon.mod.common.api.fossil.Fossil
import com.cobblemon.mod.common.api.fossil.Fossils
import com.cobblemon.mod.common.pokemon.Pokemon
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.item.ItemStack

/**
 * Query wrapper around Cobblemon's [Fossils] registry, plus safe creation of a fossil's
 * result [Pokemon].
 *
 * **Scope note:** [Fossils] is a JSON-loaded registry with no public registration entry
 * point (data loads from datapack `fossils/` resources only) — this wrapper is query +
 * result-creation only. The actual fossil-restoration-tank mechanic (multiblock UI,
 * resurrection progress) lives entirely in internal block-entity logic, not under
 * Cobblemon's public `api/` surface, so it isn't wrapped here either.
 */
object FossilManager {

    fun all(): List<Fossil> = Fossils.all()

    fun get(identifier: ResourceLocation): Fossil? = Fossils.getByIdentifier(identifier)

    /**
     * Finds the [Fossil] whose ingredient list exactly matches [ingredients] (same
     * size, each ingredient matched), via Cobblemon's own [Fossils.getFossilByItemStacks].
     */
    fun matchFossil(ingredients: List<ItemStack>): Fossil? = Fossils.getFossilByItemStacks(ingredients)

    /**
     * Checks whether [itemStack] is a valid ingredient for any registered fossil, via
     * Cobblemon's own [Fossils.isFossilIngredient].
     */
    fun isFossilIngredient(itemStack: ItemStack): Boolean = Fossils.isFossilIngredient(itemStack)

    /**
     * Creates the [Pokemon] that [fossil] resurrects into, via its [Fossil.result]
     * [PokemonProperties][com.cobblemon.mod.common.api.pokemon.PokemonProperties.create].
     * [player] is passed straight through for whatever spawn-cause attribution
     * Cobblemon's own fossil-revival flow would apply.
     */
    fun createResult(fossil: Fossil, player: ServerPlayer? = null): Pokemon = fossil.result.create(player)
}
