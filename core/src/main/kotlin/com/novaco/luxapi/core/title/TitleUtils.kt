package com.novaco.luxapi.core.title

import net.minecraft.network.chat.Component
import net.minecraft.network.protocol.game.ClientboundClearTitlesPacket
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket
import net.minecraft.server.level.ServerPlayer

/**
 * Utility for sending titles, subtitles, and action bar messages to players.
 */
object TitleUtils {

    /**
     * Sends a title/subtitle pair with the given fade-in/stay/fade-out timings (in ticks).
     */
    fun sendTitle(
        player: ServerPlayer,
        title: Component? = null,
        subtitle: Component? = null,
        fadeIn: Int = 10,
        stay: Int = 70,
        fadeOut: Int = 20
    ) {
        player.connection.send(ClientboundSetTitlesAnimationPacket(fadeIn, stay, fadeOut))
        if (subtitle != null) player.connection.send(ClientboundSetSubtitleTextPacket(subtitle))
        if (title != null) player.connection.send(ClientboundSetTitleTextPacket(title))
    }

    /**
     * Sends an action bar message above the player's hotbar.
     */
    fun sendActionBar(player: ServerPlayer, message: Component) {
        player.connection.send(ClientboundSetActionBarTextPacket(message))
    }

    /**
     * Clears any active title/subtitle. Set [resetTimes] to also restore default fade timings.
     */
    fun clear(player: ServerPlayer, resetTimes: Boolean = false) {
        player.connection.send(ClientboundClearTitlesPacket(resetTimes))
    }
}
