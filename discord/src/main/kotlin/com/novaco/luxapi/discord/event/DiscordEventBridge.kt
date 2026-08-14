package com.novaco.luxapi.discord.event

import com.novaco.luxapi.commons.event.EventBus
import net.dv8tion.jda.api.events.session.ReadyEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

/**
 * Bridges ambient JDA events onto the commons [EventBus] as [com.novaco.luxapi.commons.event.LuxEvent]s,
 * so consumer code reacts via `@Subscribe` without importing JDA types. Currently
 * bridges only bot-ready; more JDA event types can be added here as additional
 * `on...` overrides without changing this class's role as a pure transport.
 */
class DiscordEventBridge : ListenerAdapter() {

    override fun onReady(event: ReadyEvent) {
        EventBus.fire(DiscordReadyEvent(event.jda))
    }
}
