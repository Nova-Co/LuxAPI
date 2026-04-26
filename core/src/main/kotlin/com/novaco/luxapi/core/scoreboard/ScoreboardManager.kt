package com.novaco.luxapi.core.scoreboard

import net.minecraft.network.chat.Component
import net.minecraft.network.protocol.game.ClientboundResetScorePacket
import net.minecraft.network.protocol.game.ClientboundSetDisplayObjectivePacket
import net.minecraft.network.protocol.game.ClientboundSetObjectivePacket
import net.minecraft.network.protocol.game.ClientboundSetScorePacket
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.scores.DisplaySlot
import net.minecraft.world.scores.Objective
import net.minecraft.world.scores.Scoreboard
import net.minecraft.world.scores.criteria.ObjectiveCriteria
import java.util.Optional
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/**
 * Global manager for custom virtual scoreboards.
 */
object ScoreboardManager {

    private val activeScoreboards = ConcurrentHashMap<String, LuxScoreboard>()

    /**
     * Creates a new virtual scoreboard or retrieves an existing one by ID.
     */
    fun createScoreboard(id: String, title: Component): LuxScoreboard {
        return activeScoreboards.getOrPut(id) { LuxScoreboard(id, title) }
    }

    /**
     * Retrieves an active scoreboard.
     */
    fun getScoreboard(id: String): LuxScoreboard? {
        return activeScoreboards[id]
    }

    /**
     * Destroys a scoreboard, instantly removing it from all viewers' screens.
     */
    fun destroyScoreboard(id: String, playersProvider: (UUID) -> ServerPlayer?) {
        val scoreboard = activeScoreboards.remove(id)
        scoreboard?.destroy(playersProvider)
    }
}

/**
 * A packet-based Scoreboard instance.
 */
class LuxScoreboard(val id: String, private var title: Component) {

    private val dummyScoreboard = Scoreboard()
    private val objective: Objective = dummyScoreboard.addObjective(
        id,
        ObjectiveCriteria.DUMMY,
        title,
        ObjectiveCriteria.RenderType.INTEGER,
        false,
        null
    )

    private val lines = mutableMapOf<Int, String>()
    private val viewers = mutableSetOf<UUID>()

    /**
     * Updates the title of the scoreboard for all current viewers.
     */
    fun setTitle(newTitle: Component, playersProvider: (UUID) -> ServerPlayer?) {
        this.title = newTitle
        objective.displayName = newTitle
        broadcastPacket(ClientboundSetObjectivePacket(objective, 2), playersProvider)
    }

    /**
     * Sets or updates a specific line on the scoreboard.
     */
    fun setLine(index: Int, text: String, playersProvider: (UUID) -> ServerPlayer?) {
        val oldText = lines[index]

        if (oldText != null && oldText != text) {
            broadcastPacket(ClientboundResetScorePacket(oldText, objective.name), playersProvider)
        }

        lines[index] = text

        broadcastPacket(
            ClientboundSetScorePacket(
                text,
                objective.name,
                index,
                Optional.empty(),
                Optional.empty()
            ),
            playersProvider
        )
    }

    /**
     * Removes a specific line from the scoreboard.
     */
    fun removeLine(index: Int, playersProvider: (UUID) -> ServerPlayer?) {
        val text = lines.remove(index) ?: return
        broadcastPacket(ClientboundResetScorePacket(text, objective.name), playersProvider)
    }

    /**
     * Displays the scoreboard to a specific player's screen.
     */
    fun showTo(player: ServerPlayer) {
        if (viewers.contains(player.uuid)) return
        viewers.add(player.uuid)

        player.connection.send(ClientboundSetObjectivePacket(objective, 0))
        player.connection.send(ClientboundSetDisplayObjectivePacket(DisplaySlot.SIDEBAR, objective))

        lines.forEach { (index, text) ->
            player.connection.send(
                ClientboundSetScorePacket(
                    text,
                    objective.name,
                    index,
                    Optional.empty(),
                    Optional.empty()
                )
            )
        }
    }

    /**
     * Removes the scoreboard from a specific player's screen.
     */
    fun hideFrom(player: ServerPlayer) {
        if (!viewers.contains(player.uuid)) return
        viewers.remove(player.uuid)

        player.connection.send(ClientboundSetObjectivePacket(objective, 1))
    }

    /**
     * Internal cleanup method to purge the scoreboard for everyone.
     */
    internal fun destroy(playersProvider: (UUID) -> ServerPlayer?) {
        broadcastPacket(ClientboundSetObjectivePacket(objective, 1), playersProvider)
        viewers.clear()
        lines.clear()
    }

    /**
     * Utility to send a packet to all active viewers safely.
     */
    private fun broadcastPacket(packet: net.minecraft.network.protocol.Packet<*>, playersProvider: (UUID) -> ServerPlayer?) {
        viewers.forEach { uuid ->
            playersProvider(uuid)?.connection?.send(packet)
        }
    }
}