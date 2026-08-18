package com.novaco.luxapi.commons.config.gui

import com.novaco.luxapi.commons.player.LuxPlayer

/** Plays [sound] for [player] at the given [volume]/[pitch]. Supplied by platform bootstrap code. */
fun interface SoundPlayer {
    fun play(player: LuxPlayer, sound: String, volume: Float, pitch: Float)
}
