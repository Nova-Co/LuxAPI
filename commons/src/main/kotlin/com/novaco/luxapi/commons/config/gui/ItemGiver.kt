package com.novaco.luxapi.commons.config.gui

import com.novaco.luxapi.commons.config.type.ConfigItemStack
import com.novaco.luxapi.commons.player.LuxPlayer

/** Hands [player] a physical copy of [item]. Supplied by platform bootstrap code. */
fun interface ItemGiver {
    fun give(player: LuxPlayer, item: ConfigItemStack)
}
