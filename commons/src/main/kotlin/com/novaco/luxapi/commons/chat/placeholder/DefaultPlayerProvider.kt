package com.novaco.luxapi.commons.chat.placeholder

import com.novaco.luxapi.commons.player.LuxPlayer

class DefaultPlayerProvider : PlaceholderProvider {
    override fun identifier(): String = "player"

    override fun onPlaceholderRequest(player: LuxPlayer?, params: String): String? {
        if (player == null) return "Guest"
        return when (params.lowercase()) {
            "name" -> player.name
            "uuid" -> player.uniqueId.toString()
            else -> null
        }
    }
}