package com.novaco.luxapi.cobblemon.pokeball

import com.cobblemon.mod.common.api.pokeball.PokeBalls
import com.cobblemon.mod.common.api.pokeball.catching.calculators.CaptureCalculator
import com.cobblemon.mod.common.api.pokeball.catching.calculators.CaptureCalculators
import com.cobblemon.mod.common.pokeball.PokeBall
import com.cobblemon.mod.common.util.cobblemonResource

/**
 * Query wrapper around Cobblemon's [PokeBalls] registry, plus runtime registration of
 * a custom [CaptureCalculator].
 *
 * **Known gap:** [PokeBalls] itself has no working custom-registration path in this
 * Cobblemon version — its internal `custom` map exists but is only ever cleared, never
 * populated (Cobblemon's own `reload()` carries a `// ToDo once datapack pokeball is
 * implemented` comment), and a brand-new [PokeBall] instance can't be made throwable
 * without also registering a matching Minecraft `Item`, which isn't something a
 * runtime dev-API call can safely do outside mod item-registration. So "custom pokeball
 * API" as TODO.md originally framed it isn't achievable via [PokeBalls] — this wrapper
 * is query-only for the ball registry itself.
 *
 * The real extension point Cobblemon does support here is [CaptureCalculators]
 * (governs the actual catch-rate math a ball's throw resolves against), which has a
 * genuine public `register()` — that's what this wrapper exposes for customization.
 */
object PokeballManager {

    fun all(): List<PokeBall> = PokeBalls.all().toList()

    /**
     * Looks up a Pokéball by registry name (e.g. "great_ball"), assuming the
     * Cobblemon namespace. For a non-Cobblemon namespaced id, look it up via
     * [PokeBalls.getPokeBall] directly with a full [net.minecraft.resources.ResourceLocation].
     */
    fun get(name: String): PokeBall? = PokeBalls.getPokeBall(cobblemonResource(name))

    fun getCaptureCalculator(id: String): CaptureCalculator? = CaptureCalculators.fromId(id)

    /**
     * Registers a custom [CaptureCalculator] so it can be selected by id via
     * Cobblemon's own capture-calculator game rule. Note Cobblemon's own registry
     * silently replaces whatever was previously registered under the same
     * (lowercased) [CaptureCalculator.id] — check [getCaptureCalculator] first if
     * that matters.
     */
    fun registerCaptureCalculator(calculator: CaptureCalculator) {
        CaptureCalculators.register(calculator)
    }
}
