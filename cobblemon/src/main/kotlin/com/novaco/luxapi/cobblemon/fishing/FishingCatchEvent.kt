package com.novaco.luxapi.cobblemon.fishing

import com.cobblemon.mod.common.api.events.fishing.BobberSpawnPokemonEvent
import com.cobblemon.mod.common.entity.fishing.PokeRodFishingBobberEntity
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import net.minecraft.world.item.ItemStack

/**
 * A simplified view of a Cobblemon [BobberSpawnPokemonEvent.Post], as delivered by
 * [FishingInterceptor.onPokemonHooked]. Fired after a Pokémon has actually been spawned
 * by a bobber — this is the point at which the caught species is known.
 *
 * **Not cancelable:** Cobblemon's own `Post` event has no cancel function — the spawn
 * has already happened by this point. Use [FishingInterceptor.onBobberSpawnAttempt] to
 * prevent a spawn before it happens.
 *
 * @property bobber The bobber entity that produced the spawn.
 * @property bait The bait [ItemStack] that was consumed for this catch, if any.
 * @property pokemon The Pokémon entity that was spawned.
 */
data class FishingCatchEvent(
    val bobber: PokeRodFishingBobberEntity,
    val bait: ItemStack,
    val pokemon: PokemonEntity
)
