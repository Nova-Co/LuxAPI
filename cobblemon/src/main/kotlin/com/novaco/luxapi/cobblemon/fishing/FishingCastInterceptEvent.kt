package com.novaco.luxapi.cobblemon.fishing

import com.cobblemon.mod.common.api.events.fishing.PokerodCastEvent
import com.cobblemon.mod.common.entity.fishing.PokeRodFishingBobberEntity
import net.minecraft.world.item.ItemStack

/**
 * A simplified view of a Cobblemon [PokerodCastEvent.Pre], as delivered by
 * [FishingInterceptor.onCast]. Fired before a fishing rod cast starts.
 *
 * @property rod The rod [ItemStack] being cast.
 * @property bait The bait [ItemStack] currently set on the rod, if any.
 * @property bobber The bobber entity being cast.
 */
data class FishingCastInterceptEvent(
    val rod: ItemStack,
    val bait: ItemStack,
    val bobber: PokeRodFishingBobberEntity,
    private val source: PokerodCastEvent.Pre
) {
    /** Prevents the rod from being cast at all. */
    fun cancel() {
        source.cancel()
    }
}
