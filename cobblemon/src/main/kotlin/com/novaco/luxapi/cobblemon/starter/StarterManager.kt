package com.novaco.luxapi.cobblemon.starter

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.config.starter.StarterCategory
import net.minecraft.server.level.ServerPlayer

/**
 * Thin passthrough to Cobblemon's own [Cobblemon.starterHandler], which already covers
 * the real starter-selection surface (listing categories, prompting a player, and
 * finalizing a choice) — this wrapper exists so callers don't need to reach into
 * `Cobblemon.starterHandler` directly. Cobblemon's own handler can be swapped out
 * entirely (`Cobblemon.starterHandler = MyHandler()`), which this wrapper doesn't
 * attempt to replicate — that's a direct-replacement API, not a wrap-and-extend one.
 */
object StarterManager {

    fun getStarterList(player: ServerPlayer): List<StarterCategory> = Cobblemon.starterHandler.getStarterList(player)

    fun requestStarterChoice(player: ServerPlayer) {
        Cobblemon.starterHandler.requestStarterChoice(player)
    }

    fun chooseStarter(player: ServerPlayer, categoryName: String, index: Int) {
        Cobblemon.starterHandler.chooseStarter(player, categoryName, index)
    }
}
