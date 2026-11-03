package com.novaco.luxapi.neoforge.permission

import com.novaco.luxapi.commons.command.sender.CommandSender
import com.novaco.luxapi.commons.permission.PermissionChecker
import com.novaco.luxapi.commons.player.LuxPlayer
import com.novaco.luxapi.core.command.MinecraftCommandSender
import net.minecraft.server.level.ServerPlayer
import net.neoforged.neoforge.server.permission.PermissionAPI
import net.neoforged.neoforge.server.permission.events.PermissionGatherEvent
import net.neoforged.neoforge.server.permission.exceptions.UnregisteredPermissionException
import net.neoforged.neoforge.server.permission.nodes.PermissionNode
import net.neoforged.neoforge.server.permission.nodes.PermissionTypes
import org.slf4j.LoggerFactory
import java.util.concurrent.ConcurrentHashMap

/**
 * NeoForge [PermissionChecker] backed by NeoForge's `PermissionAPI`.
 *
 * NeoForge only accepts permission nodes during the server's gather event, so nodes must be
 * [declare]d before the server starts; [onGatherNodes] hands them over. The active permission
 * handler decides each declared node and may deny it even to operators. A node that was never
 * declared, was declared too late, or is checked before the handler exists falls back to the
 * vanilla op-level 4 floor, with one warning per node.
 *
 * NeoForge has no permission lookup for non-players, so console and command blocks use the
 * op-level 2 floor. A command source that was run by a player is resolved for that player.
 */
class NeoForgePermissionChecker : PermissionChecker {

    private val nodes = ConcurrentHashMap<String, PermissionNode<Boolean>>()
    private val warned = ConcurrentHashMap.newKeySet<String>()

    /**
     * Registers [node] so it can be contributed to NeoForge's gather event. Node names are
     * case-insensitive; a name without a dot is placed under the `luxapi` namespace.
     *
     * @param node The permission node, e.g. `luxapi.command.admin`.
     */
    override fun declare(node: String) {
        val key = node.lowercase()
        if (nodes.containsKey(key)) return
        val namespace = if ('.' in key) key.substringBefore('.') else DEFAULT_NAMESPACE
        val path = if ('.' in key) key.substringAfter('.') else key
        val resolver = PermissionNode.PermissionResolver<Boolean> { player, _, _ -> player != null && opFloor(player) }
        nodes.putIfAbsent(key, PermissionNode(namespace, path, PermissionTypes.BOOLEAN, resolver))
    }

    /**
     * Contributes every declared node to NeoForge's permission gather event. Subscribe this to
     * `NeoForge.EVENT_BUS`.
     *
     * @param event The gather event posted when the server initializes its permission handler.
     */
    fun onGatherNodes(event: PermissionGatherEvent.Nodes) {
        event.addNodes(nodes.values)
    }

    /**
     * Resolves [node] for a player, or for a command source run by a player. Console and command
     * blocks use the op-level 2 floor.
     *
     * @param sender A [LuxPlayer] wrapping a [ServerPlayer], or a [MinecraftCommandSender].
     * @param node The permission node to check.
     * @return true if granted; false if denied or if the sender type is not recognised.
     */
    override fun has(sender: CommandSender, node: String): Boolean {
        val player = (sender as? LuxPlayer)?.parent as? ServerPlayer
        if (player != null) return resolve(player, node)
        if (sender is MinecraftCommandSender) {
            val runner = sender.source.entity as? ServerPlayer
            if (runner != null) return resolve(runner, node)
            return sender.source.hasPermission(NON_PLAYER_FLOOR_LEVEL)
        }
        return false
    }

    private fun resolve(player: ServerPlayer, node: String): Boolean {
        val key = node.lowercase()
        val permissionNode = nodes[key]
        if (permissionNode == null) {
            warnOnce(key, "Permission node '{}' was never declared; using the op-level floor.")
            return opFloor(player)
        }
        if (PermissionAPI.getActivePermissionHandler() == null) return opFloor(player)
        return try {
            PermissionAPI.getPermission(player, permissionNode)
        } catch (e: UnregisteredPermissionException) {
            warnOnce(key, "Permission node '{}' was declared after NeoForge gathered nodes; using the op-level floor.")
            opFloor(player)
        }
    }

    private fun opFloor(player: ServerPlayer): Boolean =
        player.createCommandSourceStack().hasPermission(PLAYER_FLOOR_LEVEL)

    private fun warnOnce(key: String, message: String) {
        if (warned.add(key)) logger.warn(message, key)
    }

    private companion object {
        const val DEFAULT_NAMESPACE = "luxapi"
        const val PLAYER_FLOOR_LEVEL = 4
        const val NON_PLAYER_FLOOR_LEVEL = 2
        val logger = LoggerFactory.getLogger(NeoForgePermissionChecker::class.java)
    }
}
