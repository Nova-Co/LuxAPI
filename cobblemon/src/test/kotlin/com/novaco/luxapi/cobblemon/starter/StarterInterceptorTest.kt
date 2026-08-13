package com.novaco.luxapi.cobblemon.starter

import com.cobblemon.mod.common.api.events.CobblemonEvents
import com.cobblemon.mod.common.api.events.starter.StarterChosenEvent
import com.cobblemon.mod.common.api.pokemon.PokemonProperties
import com.cobblemon.mod.common.api.reactive.ObservableSubscription
import com.cobblemon.mod.common.pokemon.Pokemon
import net.minecraft.SharedConstants
import net.minecraft.server.Bootstrap
import net.minecraft.server.level.ServerPlayer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock

class StarterInterceptorTest {

    companion object {
        @JvmStatic
        @BeforeAll
        fun bootstrapMinecraft() {
            SharedConstants.tryDetectVersion()
            Bootstrap.bootStrap()
        }
    }

    // See DropInterceptorTest's comment: Cobblemon's event bus has no test-scoped
    // isolation, so every subscription made here is torn down after its test.
    private var subscription: ObservableSubscription<StarterChosenEvent>? = null

    @AfterEach
    fun unsubscribe() {
        subscription?.unsubscribe()
        subscription = null
    }

    @Test
    fun `onStarterChosen delivers the player and pokemon`() {
        val player = mock<ServerPlayer>()
        val pokemon = mock<Pokemon>()
        val properties = PokemonProperties()
        val received = mutableListOf<StarterChosenInterceptEvent>()

        subscription = StarterInterceptor.onStarterChosen { received.add(it) }
        CobblemonEvents.STARTER_CHOSEN.post(StarterChosenEvent(player, properties, pokemon))

        assertEquals(1, received.size)
        assertEquals(player, received[0].player)
        assertEquals(pokemon, received[0].pokemon)
    }

    @Test
    fun `replacePokemon swaps the pokemon on the underlying Cobblemon event`() {
        val player = mock<ServerPlayer>()
        val original = mock<Pokemon>()
        val replacement = mock<Pokemon>()
        val event = StarterChosenEvent(player, PokemonProperties(), original)

        subscription = StarterInterceptor.onStarterChosen { it.replacePokemon(replacement) }
        CobblemonEvents.STARTER_CHOSEN.post(event)

        assertEquals(replacement, event.pokemon)
    }

    @Test
    fun `cancel on the delivered event cancels the underlying Cobblemon event`() {
        val player = mock<ServerPlayer>()
        val pokemon = mock<Pokemon>()
        val event = StarterChosenEvent(player, PokemonProperties(), pokemon)

        subscription = StarterInterceptor.onStarterChosen { it.cancel() }
        CobblemonEvents.STARTER_CHOSEN.post(event)

        assertTrue(event.isCanceled)
    }
}
