package com.novaco.luxapi.discord.event

import com.novaco.luxapi.commons.event.EventBus
import com.novaco.luxapi.commons.event.Subscribe
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.events.session.ReadyEvent
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.mockito.kotlin.mock

class DiscordEventBridgeTest {

    @AfterEach
    fun tearDown() {
        EventBus.clear()
    }

    class CapturingListener {
        var captured: DiscordReadyEvent? = null

        @Subscribe
        fun onReady(event: DiscordReadyEvent) {
            captured = event
        }
    }

    @Test
    fun `test onReady fires a DiscordReadyEvent on the EventBus`() {
        val listener = CapturingListener()
        EventBus.register(listener)

        val jda = mock<JDA>()
        val readyEvent = mock<ReadyEvent>()
        `when`(readyEvent.jda).thenReturn(jda)

        DiscordEventBridge().onReady(readyEvent)

        assertNotNull(listener.captured)
        assertSame(jda, listener.captured?.jda)
    }
}
