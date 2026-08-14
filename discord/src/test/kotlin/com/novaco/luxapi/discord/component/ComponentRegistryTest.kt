package com.novaco.luxapi.discord.component

import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.mockito.kotlin.mock

class ComponentRegistryTest {

    @AfterEach
    fun tearDown() {
        ComponentRegistry.clear()
    }

    @Test
    fun `test registered button handler is invoked on dispatch`() {
        var invoked = false
        ComponentRegistry.registerButton("test-button") { invoked = true }

        val event = mock<ButtonInteractionEvent>()
        `when`(event.componentId).thenReturn("test-button")

        ComponentRegistry.dispatchButton(event)

        assertTrue(invoked)
    }

    @Test
    fun `test dispatching an unknown button id does not throw`() {
        val event = mock<ButtonInteractionEvent>()
        `when`(event.componentId).thenReturn("unknown")

        assertDoesNotThrow { ComponentRegistry.dispatchButton(event) }
    }

    @Test
    fun `test a throwing button handler replies ephemeral instead of propagating`() {
        ComponentRegistry.registerButton("boom") { throw IllegalStateException("boom") }

        val event = mock<ButtonInteractionEvent>()
        `when`(event.componentId).thenReturn("boom")
        val replyAction = mock<ReplyCallbackAction>()
        `when`(event.reply(anyString())).thenReturn(replyAction)
        `when`(replyAction.setEphemeral(anyBoolean())).thenReturn(replyAction)

        assertDoesNotThrow { ComponentRegistry.dispatchButton(event) }
        verify(event).reply("An internal error occurred while handling this interaction.")
    }

    @Test
    fun `test registered modal handler is invoked on dispatch`() {
        var invoked = false
        ComponentRegistry.registerModal("test-modal") { invoked = true }

        val event = mock<ModalInteractionEvent>()
        `when`(event.modalId).thenReturn("test-modal")

        ComponentRegistry.dispatchModal(event)

        assertTrue(invoked)
    }
}
