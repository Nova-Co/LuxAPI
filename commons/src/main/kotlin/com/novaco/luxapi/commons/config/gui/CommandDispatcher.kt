package com.novaco.luxapi.commons.config.gui

import com.novaco.luxapi.commons.player.LuxPlayer

/** Runs a command string on behalf of [player]. Supplied by platform bootstrap code. */
fun interface CommandDispatcher {
    fun dispatch(player: LuxPlayer, command: String)
}
