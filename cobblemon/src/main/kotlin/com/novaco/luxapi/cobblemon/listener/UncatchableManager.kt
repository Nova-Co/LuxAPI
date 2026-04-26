package com.novaco.luxapi.cobblemon.listener

import com.cobblemon.mod.common.api.events.CobblemonEvents
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import net.minecraft.server.level.ServerPlayer

/**
 * Logic engine to enforce uncatchable rules for specific Pokémon.
 * Listens for Poké Ball impacts and cancels the capture sequence if the target has restricted tags.
 */
object UncatchableManager {

    /**
     * Optional callback allowing developers to define custom feedback (e.g., messages, sounds, particles)
     * when a capture attempt is successfully blocked.
     */
    var onCaptureBlocked: ((ServerPlayer, PokemonEntity) -> Unit)? = null

    // Prevent multiple registrations
    private var isRegistered = false

    /**
     * Initializes the listener. Must be called during the mod's initialization phase.
     */
    fun register() {
        if (isRegistered) return

        CobblemonEvents.THROWN_POKEBALL_HIT.subscribe { event ->
            val targetEntity = event.pokemon

            // Check if the entity has our custom uncatchable tags
            if (targetEntity.tags.contains("lux_uncatchable") || targetEntity.tags.contains("lux_is_boss")) {

                if (targetEntity.isBattling) {
                    return@subscribe
                }

                // Immediately cancel the event, causing the Poké Ball to bounce off
                event.cancel()

                // Execute developer-defined feedback logic, if provided
                val player = event.pokeBall.owner as? ServerPlayer
                if (player != null) {
                    onCaptureBlocked?.invoke(player, targetEntity)
                }
            }
        }
        isRegistered = true
    }
}