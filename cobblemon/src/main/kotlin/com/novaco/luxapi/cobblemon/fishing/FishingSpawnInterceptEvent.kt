package com.novaco.luxapi.cobblemon.fishing

import com.cobblemon.mod.common.api.events.fishing.BobberSpawnPokemonEvent
import com.cobblemon.mod.common.api.spawning.detail.SpawnAction
import com.cobblemon.mod.common.entity.fishing.PokeRodFishingBobberEntity
import net.minecraft.world.item.ItemStack

/**
 * A simplified view of a Cobblemon [BobberSpawnPokemonEvent.Pre], as delivered by
 * [FishingInterceptor.onBobberSpawnAttempt]. Fired before a bobber spawns a Pokémon in.
 *
 * **Known gap:** the underlying Cobblemon event has no resolved species at this stage —
 * [spawnAction] is Cobblemon's own planned-spawn object and may be inspected for more
 * detail, but this wrapper doesn't attempt to pre-resolve a species name here. Use
 * [FishingInterceptor.onPokemonHooked] instead if you need the actual caught species.
 *
 * @property bobber The bobber entity about to spawn a Pokémon.
 * @property rod The rod [ItemStack] the bobber is attached to.
 * @property spawnAction Cobblemon's own planned spawn, for callers that need more detail.
 */
data class FishingSpawnInterceptEvent(
    val bobber: PokeRodFishingBobberEntity,
    val rod: ItemStack,
    val spawnAction: SpawnAction<*>,
    private val source: BobberSpawnPokemonEvent.Pre
) {
    /** Prevents this bobber spawn attempt from producing a Pokémon. */
    fun cancel() {
        source.cancel()
    }
}
