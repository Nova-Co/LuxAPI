package com.novaco.luxapi.commons.event.player

import com.novaco.luxapi.commons.event.LuxEvent
import com.novaco.luxapi.commons.player.LuxPlayer

/**
 * Event triggered when a player disconnects from the server.
 */
class PlayerQuitEvent(val player: LuxPlayer) : LuxEvent