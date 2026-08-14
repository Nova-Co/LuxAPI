package com.novaco.luxapi.cobblemon.trade

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.CobblemonImplementation
import com.cobblemon.mod.common.api.events.CobblemonEvents
import com.cobblemon.mod.common.api.events.pokemon.TradeEvent
import com.cobblemon.mod.common.api.reactive.ObservableSubscription
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.trade.TradeParticipant
import net.minecraft.SharedConstants
import net.minecraft.server.Bootstrap
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.util.UUID

class TradeInterceptorTest {

    companion object {
        @JvmStatic
        @BeforeAll
        fun bootstrapMinecraft() {
            SharedConstants.tryDetectVersion()
            Bootstrap.bootStrap()
        }
    }

    // TradeEvent.Pre/Post construction reads Cobblemon.implementation.server() internally
    // (via TradeParticipant.uuid.getPlayer()). Cobblemon.implementation is a lateinit var
    // never set outside a running platform, so it must be stubbed before construction.
    @BeforeEach
    fun stubCobblemonImplementation() {
        val implementation = mock<CobblemonImplementation>()
        whenever(implementation.server()).thenReturn(null)
        Cobblemon.implementation = implementation
    }

    private var preSubscription: ObservableSubscription<TradeEvent.Pre>? = null
    private var postSubscription: ObservableSubscription<TradeEvent.Post>? = null

    @AfterEach
    fun unsubscribe() {
        preSubscription?.unsubscribe()
        postSubscription?.unsubscribe()
        preSubscription = null
        postSubscription = null
    }

    private fun participant(): TradeParticipant {
        val participant = mock<TradeParticipant>()
        whenever(participant.uuid).thenReturn(UUID.randomUUID())
        return participant
    }

    @Test
    fun `onTradeAttempt delivers both participants and pokemon`() {
        val p1 = participant()
        val p2 = participant()
        val pokemon1 = mock<Pokemon>()
        val pokemon2 = mock<Pokemon>()
        val received = mutableListOf<TradeInterceptEvent>()

        preSubscription = TradeInterceptor.onTradeAttempt { received.add(it) }
        CobblemonEvents.TRADE_EVENT_PRE.post(TradeEvent.Pre(p1, pokemon1, p2, pokemon2))

        assertEquals(1, received.size)
        assertEquals(p1.uuid, received[0].participant1Uuid)
        assertEquals(pokemon1, received[0].participant1Pokemon)
        assertEquals(p2.uuid, received[0].participant2Uuid)
        assertEquals(pokemon2, received[0].participant2Pokemon)
    }

    @Test
    fun `cancel on the delivered event cancels the underlying trade`() {
        val event = TradeEvent.Pre(participant(), mock(), participant(), mock())

        preSubscription = TradeInterceptor.onTradeAttempt { it.cancel() }
        CobblemonEvents.TRADE_EVENT_PRE.post(event)

        assertTrue(event.isCanceled)
    }

    @Test
    fun `onTradeCompleted delivers both participants and pokemon after a trade`() {
        val p1 = participant()
        val p2 = participant()
        val pokemon1 = mock<Pokemon>()
        val pokemon2 = mock<Pokemon>()
        val received = mutableListOf<TradeCompletedEvent>()

        postSubscription = TradeInterceptor.onTradeCompleted { received.add(it) }
        CobblemonEvents.TRADE_EVENT_POST.post(TradeEvent.Post(p1, pokemon1, p2, pokemon2))

        assertEquals(1, received.size)
        assertEquals(p1.uuid, received[0].participant1Uuid)
        assertEquals(pokemon1, received[0].participant1Pokemon)
        assertEquals(p2.uuid, received[0].participant2Uuid)
        assertEquals(pokemon2, received[0].participant2Pokemon)
    }
}
