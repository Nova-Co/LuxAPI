package com.novaco.luxapi.cobblemon.tag

import com.cobblemon.mod.common.api.pokemon.PokemonSpecies
import com.cobblemon.mod.common.pokemon.Species

/**
 * Query wrapper over Cobblemon's species/form label system — what TODO.md's "species/type
 * tag queries" maps to in real Cobblemon terms. There is no separate `tags` registry;
 * labels (`Species.labels`/`FormData.labels`) are the mechanism, and
 * `com.cobblemon.mod.common.api.pokemon.labels.CobblemonPokemonLabels` documents the
 * built-in constant set (`legendary`, `mythical`, `gen1`-`gen9`, etc).
 *
 * **Scope note:** for a live [com.cobblemon.mod.common.pokemon.Pokemon], call its own
 * public `hasLabels(vararg)` directly rather than going through this wrapper — it
 * already checks exactly this against the Pokémon's current form. This object covers
 * registry-level species queries only.
 */
object TagManager {

    /**
     * The labels declared on [species] (its base labels — an individual form can
     * override these, see [com.cobblemon.mod.common.pokemon.FormData.labels]).
     */
    fun labelsOf(species: Species): Set<String> = species.labels

    fun hasLabel(species: Species, label: String): Boolean =
        species.labels.any { it.equals(label, ignoreCase = true) }

    /**
     * All implemented [Species] carrying [label], via [PokemonSpecies.implemented].
     */
    fun speciesWithLabel(label: String): List<Species> =
        PokemonSpecies.implemented.filter { hasLabel(it, label) }
}
