package com.novaco.luxapi.cobblemon.spawning

import com.cobblemon.mod.common.api.habitats.HabitatPool
import com.cobblemon.mod.common.api.habitats.HabitatPools
import net.minecraft.resources.ResourceLocation

/**
 * Query wrapper around Cobblemon's [HabitatPools] registry (the habitat-block spawning
 * subsystem).
 *
 * **Scope note:** [HabitatPools] is JSON-loaded (datapack `habitat_pools/` resources)
 * plus a world-saved-pools folder loaded from disk, with no public runtime
 * registration entry point — this wrapper is query-only, the same shape
 * [com.novaco.luxapi.cobblemon.fossil.FossilManager] uses for its own JSON-loaded
 * registry.
 */
object HabitatManager {

    fun all(): Collection<HabitatPool> = HabitatPools.habitatPoolsById.values

    fun get(identifier: ResourceLocation): HabitatPool? = HabitatPools.habitatPoolsById[identifier]
}
