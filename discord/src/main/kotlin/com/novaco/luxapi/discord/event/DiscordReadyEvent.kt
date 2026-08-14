package com.novaco.luxapi.discord.event

import com.novaco.luxapi.commons.event.LuxEvent
import net.dv8tion.jda.api.JDA

/**
 * Fired on the [com.novaco.luxapi.commons.event.EventBus] once the Discord bot has
 * finished its initial connection and is ready to receive interactions.
 */
class DiscordReadyEvent(val jda: JDA) : LuxEvent
