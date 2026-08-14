package com.novaco.luxapi.cobblemon.riding

import com.cobblemon.mod.common.api.events.CobblemonEvents
import com.cobblemon.mod.common.api.events.pokemon.RidePokemonEvent
import com.cobblemon.mod.common.api.reactive.ObservableSubscription
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import net.minecraft.SharedConstants
import net.minecraft.server.Bootstrap
import net.minecraft.server.level.ServerPlayer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock

class RidingInterceptorTest {

    companion object {
        @JvmStatic
        @BeforeAll
        fun bootstrapMinecraft() {
            SharedConstants.tryDetectVersion()
            Bootstrap.bootStrap()
        }
    }

    private var preSub: ObservableSubscription<RidePokemonEvent.Pre>? = null
    private var postSub: ObservableSubscription<RidePokemonEvent.Post>? = null
    private var staminaSub: ObservableSubscription<RidePokemonEvent.ApplyStamina>? = null

    @AfterEach
    fun unsubscribe() {
        preSub?.unsubscribe(); postSub?.unsubscribe(); staminaSub?.unsubscribe()
        preSub = null; postSub = null; staminaSub = null
    }

    @Test
    fun `onRideAttempt delivers the player and pokemon`() {
        val player = mock<ServerPlayer>()
        val pokemon = mock<PokemonEntity>()
        val received = mutableListOf<RideAttemptEvent>()

        preSub = RidingInterceptor.onRideAttempt { received.add(it) }
        CobblemonEvents.RIDE_EVENT_PRE.post(RidePokemonEvent.Pre(player, pokemon))

        assertEquals(1, received.size)
        assertEquals(player, received[0].player)
        assertEquals(pokemon, received[0].pokemon)
    }

    @Test
    fun `cancel on the delivered ride attempt cancels the underlying event`() {
        val event = RidePokemonEvent.Pre(mock(), mock())

        preSub = RidingInterceptor.onRideAttempt { it.cancel() }
        CobblemonEvents.RIDE_EVENT_PRE.post(event)

        assertTrue(event.isCanceled)
    }

    @Test
    fun `onRideCompleted delivers the player and pokemon`() {
        val player = mock<ServerPlayer>()
        val pokemon = mock<PokemonEntity>()
        val received = mutableListOf<RideCompletedEvent>()

        postSub = RidingInterceptor.onRideCompleted { received.add(it) }
        CobblemonEvents.RIDE_EVENT_POST.post(RidePokemonEvent.Post(player, pokemon))

        assertEquals(1, received.size)
        assertEquals(player, received[0].player)
        assertEquals(pokemon, received[0].pokemon)
    }

    @Test
    fun `onStaminaApply exposes the current stamina value`() {
        val received = mutableListOf<RideStaminaEvent>()

        staminaSub = RidingInterceptor.onStaminaApply { received.add(it) }
        CobblemonEvents.RIDE_EVENT_APPLY_STAMINA.post(RidePokemonEvent.ApplyStamina(mock(), mock(), 0.5F))

        assertEquals(1, received.size)
        assertEquals(0.5F, received[0].stamina)
    }

    @Test
    fun `mutating stamina on the delivered event changes the underlying event's stamina`() {
        val event = RidePokemonEvent.ApplyStamina(mock(), mock(), 0.5F)

        staminaSub = RidingInterceptor.onStaminaApply { it.stamina = 0.1F }
        CobblemonEvents.RIDE_EVENT_APPLY_STAMINA.post(event)

        assertEquals(0.1F, event.rideStamina)
    }

    @Test
    fun `setInfiniteStamina sets the underlying event's stamina to -1`() {
        val event = RidePokemonEvent.ApplyStamina(mock(), mock(), 0.5F)

        staminaSub = RidingInterceptor.onStaminaApply { it.setInfiniteStamina() }
        CobblemonEvents.RIDE_EVENT_APPLY_STAMINA.post(event)

        assertEquals(-1F, event.rideStamina)
    }
}
