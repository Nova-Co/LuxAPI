package com.novaco.luxapi.discord.listener

import com.novaco.luxapi.discord.command.DiscordCommandManager
import com.novaco.luxapi.discord.component.ComponentRegistry
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

/**
 * Routes JDA interaction events to the [DiscordCommandManager] and [ComponentRegistry].
 */
class InteractionListener(private val commandManager: DiscordCommandManager) : ListenerAdapter() {

    override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) {
        commandManager.dispatch(event)
    }

    override fun onButtonInteraction(event: ButtonInteractionEvent) {
        ComponentRegistry.dispatchButton(event)
    }

    override fun onModalInteraction(event: ModalInteractionEvent) {
        ComponentRegistry.dispatchModal(event)
    }
}
