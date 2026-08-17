package com.novaco.luxapi.discord.component

import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import org.slf4j.LoggerFactory
import java.util.concurrent.ConcurrentHashMap

/**
 * The central registry for Discord button and modal interaction handlers, keyed by
 * their JDA custom-id. Buttons and modals are callback-driven rather than
 * annotation-driven, so this is a plain registry rather than a reflection processor.
 */
object ComponentRegistry {

    private val logger = LoggerFactory.getLogger(ComponentRegistry::class.java)

    private val buttonHandlers = ConcurrentHashMap<String, (ButtonInteractionEvent) -> Unit>()
    private val modalHandlers = ConcurrentHashMap<String, (ModalInteractionEvent) -> Unit>()

    /** Registers a handler for a button with the given custom-id. */
    fun registerButton(id: String, handler: (ButtonInteractionEvent) -> Unit) {
        buttonHandlers[id] = handler
    }

    /** Registers a handler for a modal with the given custom-id. */
    fun registerModal(id: String, handler: (ModalInteractionEvent) -> Unit) {
        modalHandlers[id] = handler
    }

    /**
     * Routes a button interaction to its registered handler. Logs and returns instead
     * of throwing if no handler is registered for the button's id. A handler exception
     * is caught, logged, and answered with a generic ephemeral error.
     */
    fun dispatchButton(event: ButtonInteractionEvent) {
        val handler = buttonHandlers[event.componentId]
        if (handler == null) {
            logger.warn("Received interaction for unregistered button id '{}'", event.componentId)
            return
        }
        try {
            handler(event)
        } catch (e: RuntimeException) {
            logger.error("Unhandled exception while handling button '{}'", event.componentId, e)
            event.reply("An internal error occurred while handling this interaction.").setEphemeral(true).queue()
        }
    }

    /**
     * Routes a modal interaction to its registered handler. Same error-handling shape
     * as [dispatchButton].
     */
    fun dispatchModal(event: ModalInteractionEvent) {
        val handler = modalHandlers[event.modalId]
        if (handler == null) {
            logger.warn("Received interaction for unregistered modal id '{}'", event.modalId)
            return
        }
        try {
            handler(event)
        } catch (e: RuntimeException) {
            logger.error("Unhandled exception while handling modal '{}'", event.modalId, e)
            event.reply("An internal error occurred while handling this interaction.").setEphemeral(true).queue()
        }
    }

    /** Clears all registered handlers. Intended for test teardown. */
    fun clear() {
        buttonHandlers.clear()
        modalHandlers.clear()
    }
}
