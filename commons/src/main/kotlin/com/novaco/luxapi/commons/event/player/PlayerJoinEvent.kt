package com.novaco.luxapi.commons.event.player

import com.novaco.luxapi.commons.event.LuxEvent
import com.novaco.luxapi.commons.player.LuxPlayer

/**
 * Event triggered when a player successfully joins the server.
 */
class PlayerJoinEvent(val player: LuxPlayer) : LuxEvent