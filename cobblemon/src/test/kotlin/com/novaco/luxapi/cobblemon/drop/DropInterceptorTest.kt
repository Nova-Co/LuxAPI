package com.novaco.luxapi.cobblemon.drop

import com.cobblemon.mod.common.api.drop.DropEntry
import com.cobblemon.mod.common.api.drop.DropTable
import com.cobblemon.mod.common.api.drop.ItemDropEntry
import com.cobblemon.mod.common.api.events.CobblemonEvents
import com.cobblemon.mod.common.api.events.drops.LootDroppedEvent
import com.cobblemon.mod.common.api.reactive.ObservableSubscription
import net.minecraft.SharedConstants
import net.minecraft.server.Bootstrap
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test

class DropInterceptorTest {

    companion object {
        @JvmStatic
        @BeforeAll
        fun bootstrapMinecraft() {
            SharedConstants.tryDetectVersion()
            Bootstrap.bootStrap()
        }
    }

    // Cobblemon's event bus has no test-scoped isolation — a listener left subscribed
    // after a test would keep firing (and could short-circuit later tests via
    // cancellation) for the rest of the JVM's lifetime, so every subscription made here
    // is torn down immediately after its test.
    private var subscription: ObservableSubscription<LootDroppedEvent>? = null

    @AfterEach
    fun unsubscribe() {
        subscription?.unsubscribe()
        subscription = null
    }

    @Test
    fun `onLootDropped delivers the drop list and table`() {
        val table = DropTable()
        val drops = mutableListOf<DropEntry>(ItemDropEntry())
        val received = mutableListOf<DropInterceptEvent>()

        subscription = DropInterceptor.onLootDropped { received.add(it) }
        CobblemonEvents.LOOT_DROPPED.post(LootDroppedEvent(table, null, null, drops))

        assertEquals(1, received.size)
        assertEquals(table, received[0].table)
        assertEquals(drops, received[0].drops)
    }

    @Test
    fun `cancel on the delivered event cancels the underlying Cobblemon event`() {
        val table = DropTable()
        val event = LootDroppedEvent(table, null, null, mutableListOf())

        subscription = DropInterceptor.onLootDropped { it.cancel() }
        CobblemonEvents.LOOT_DROPPED.post(event)

        assertTrue(event.isCanceled)
    }
}
