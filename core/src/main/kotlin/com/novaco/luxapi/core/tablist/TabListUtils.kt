package com.novaco.luxapi.core.tablist

import net.minecraft.network.chat.Component
import net.minecraft.network.protocol.game.ClientboundTabListPacket
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer

/**
 * Utility for setting the tab list (player list) header and footer.
 */
object TabListUtils {

    /**
     * Sets the tab list header/footer for a single player.
     */
    fun setHeaderFooter(player: ServerPlayer, header: Component, footer: Component) {
        player.connection.send(ClientboundTabListPacket(header, footer))
    }

    /**
     * Sets the same tab list header/footer for every player currently online.
     */
    fun broadcastHeaderFooter(server: MinecraftServer, header: Component, footer: Component) {
        val packet = ClientboundTabListPacket(header, footer)
        server.playerList.players.forEach { it.connection.send(packet) }
    }
}
