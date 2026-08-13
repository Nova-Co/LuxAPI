package com.novaco.luxapi.cobblemon.cooking

import com.cobblemon.mod.common.api.berry.Berries
import com.cobblemon.mod.common.api.berry.Berry
import com.cobblemon.mod.common.api.conditional.RegistryLikeIdentifierCondition
import com.cobblemon.mod.common.api.cooking.Flavour
import com.cobblemon.mod.common.api.cooking.Seasoning
import com.cobblemon.mod.common.api.cooking.Seasonings
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.DyeColor
import net.minecraft.world.item.ItemStack

/**
 * Query wrapper around Cobblemon's [Berries] registry, plus a real registration point
 * for custom curry/Poké Puff ingredients via [Seasonings].
 *
 * **Package placement note:** TODO.md originally suggested folding `cooking`/`berry`/
 * `mulch` into `cobblemon/economy` since none of them had a standalone package yet —
 * that doesn't hold up against the real API: none of this touches LuxAPI's virtual
 * currency (`LuxPokemonEconomy`), it's item-flavour/food data, so it gets its own
 * package here instead, consistent with every other Phase 11 subsystem so far.
 *
 * **Scope note:** [Berries] is JSON-loaded and fully read-only (`reload` is the only
 * populator, no public register point at all, unlike `Abilities`/`ElementalTypes`) —
 * a berry can only be queried here, not created at runtime. [Seasonings], however,
 * exposes its backing list (`Seasonings.seasonings`) as a public mutable property with
 * no encapsulation, which [registerSeasoning] uses as a real (if unusually loose)
 * registration point — Cobblemon itself populates that same list from both JSON data
 * and berry-derived seasonings at reload time.
 *
 * **Not covered:** [com.cobblemon.mod.common.api.mulch.MulchVariant] is a fixed enum
 * (8 variants + NONE) with no registration surface at all — already fully public and
 * trivial to use directly, wrapping it here would be dead passthrough.
 */
object CookingManager {

    fun getBerry(name: String): Berry? = Berries.getByName(name)

    fun allBerries(): List<Berry> = Berries.all()

    /**
     * Returns the combined flavour profile Cobblemon would use for [stack] in a curry
     * or Poké Puff (registered [Seasoning] plus any inherent item flavour component),
     * or null if [stack] has no flavour data at all.
     */
    fun getFlavours(stack: ItemStack): Map<Flavour, Int>? = Seasonings.getFlavoursFromItemStack(stack)

    /**
     * Registers a new [Seasoning] for the item at [itemId] via Cobblemon's own
     * (unencapsulated) [Seasonings.seasonings] list. Unlike a proper registry,
     * Cobblemon performs no duplicate check here — registering the same [itemId]
     * twice adds two entries, and [Seasonings.getFromItemStack] returns whichever
     * matches first.
     */
    fun registerSeasoning(itemId: ResourceLocation, flavours: Map<Flavour, Int>, colour: DyeColor): Seasoning {
        val seasoning = Seasoning(
            ingredient = RegistryLikeIdentifierCondition(itemId),
            flavours = flavours,
            colour = colour
        )
        Seasonings.seasonings.add(seasoning)
        return seasoning
    }
}
