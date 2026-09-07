package com.novaco.luxapi.cobblemon.npc.party

import com.cobblemon.mod.common.CobblemonPartyCompositions
import com.cobblemon.mod.common.CobblemonPartyPools
import com.cobblemon.mod.common.api.npc.PartyComposition
import com.cobblemon.mod.common.api.npc.PartyPool
import net.minecraft.resources.ResourceLocation

/**
 * Query wrapper around Cobblemon's [CobblemonPartyPools]/[CobblemonPartyCompositions]
 * registries — the datapack-driven building blocks behind
 * [com.cobblemon.mod.common.api.npc.partyproviders.ComposedPoolPartyProvider], built via
 * [PartyBuilder.useComposition].
 *
 * **Scope note:** both registries are JSON-loaded with no public registration entry
 * point (data loads from datapack `party_pools/`/`party_compositions/` resources only) —
 * this wrapper is query-only, the same shape [com.novaco.luxapi.cobblemon.fossil.FossilManager]
 * uses for its own JSON-loaded registry.
 */
object PartyCompositionManager {

    fun allPools(): Collection<PartyPool> = CobblemonPartyPools.partyPools.values

    fun getPool(identifier: ResourceLocation): PartyPool? = CobblemonPartyPools.partyPools[identifier]

    fun allCompositions(): Collection<PartyComposition> = CobblemonPartyCompositions.partyCompositions.values

    fun getComposition(identifier: ResourceLocation): PartyComposition? =
        CobblemonPartyCompositions.partyCompositions[identifier]
}
