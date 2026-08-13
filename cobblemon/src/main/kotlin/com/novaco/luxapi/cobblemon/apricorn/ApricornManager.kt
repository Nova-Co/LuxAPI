package com.novaco.luxapi.cobblemon.apricorn

import com.cobblemon.mod.common.api.apricorn.Apricorn
import com.cobblemon.mod.common.api.events.CobblemonEvents
import com.cobblemon.mod.common.api.events.farming.ApricornHarvestEvent
import com.cobblemon.mod.common.block.ApricornBlock
import com.cobblemon.mod.common.item.ApricornItem
import net.minecraft.world.item.BlockItem

/**
 * Query wrapper around Cobblemon's [Apricorn] varieties (the apricorn/pokeball
 * crafting mechanic's raw materials), plus a hook into harvest events.
 *
 * **Scope note:** [Apricorn] is a fixed 7-value enum (no registry, no runtime
 * addition of new colors), and its actual crafting recipe (apricorn -> pokeball) is a
 * vanilla Minecraft crafting-table recipe, not a Cobblemon `api` hook — there's nothing
 * to intercept there. [CobblemonEvents.APRICORN_HARVESTED] is real and is what this
 * wrapper hooks into, but unlike [com.novaco.luxapi.cobblemon.spawning.SpawnInterceptor]'s
 * event it isn't cancelable (Cobblemon's own [ApricornHarvestEvent] carries no
 * cancel state) — this is a notification hook, not a gate.
 */
object ApricornManager {

    fun all(): List<Apricorn> = Apricorn.entries

    fun get(name: String): Apricorn? = Apricorn.entries.firstOrNull { it.name.equals(name, ignoreCase = true) }

    fun itemFor(apricorn: Apricorn): ApricornItem = apricorn.item()

    fun blockFor(apricorn: Apricorn): ApricornBlock = apricorn.block()

    fun seedFor(apricorn: Apricorn): BlockItem = apricorn.seed()

    /**
     * Registers a listener that runs every time a player harvests an apricorn from a
     * tree. Purely a notification hook — [ApricornHarvestEvent] has no cancel state,
     * so a listener can observe but not prevent the harvest.
     */
    fun onApricornHarvested(listener: (ApricornHarvestEvent) -> Unit) {
        CobblemonEvents.APRICORN_HARVESTED.subscribe { event -> listener(event) }
    }
}
