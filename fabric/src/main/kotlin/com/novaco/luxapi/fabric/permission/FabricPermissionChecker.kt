package com.novaco.luxapi.fabric.permission

import com.novaco.luxapi.commons.command.sender.CommandSender
import com.novaco.luxapi.commons.permission.PermissionChecker
import com.novaco.luxapi.commons.player.LuxPlayer
import com.novaco.luxapi.core.command.MinecraftCommandSender
import me.lucko.fabric.api.permissions.v0.Permissions
import net.minecraft.commands.CommandSourceStack
import net.minecraft.server.level.ServerPlayer

/**
 * Fabric [PermissionChecker] backed by `fabric-permissions-api`.
 *
 * A permission provider such as LuckPerms is consulted first and may grant or deny any node,
 * including to operators. When no provider has an opinion the vanilla op-level floor applies:
 * level 4 for players, level 2 for console and command blocks. If the permissions API is missing
 * at runtime the floor is used on its own.
 */
class FabricPermissionChecker : PermissionChecker {

    /**
     * Resolves [node] for a player or a console/command-block sender.
     *
     * @param sender A [LuxPlayer] wrapping a [ServerPlayer], or a [MinecraftCommandSender].
     * @param node The permission node to check.
     * @return true if granted; false if denied or if the sender type is not recognised.
     */
    override fun has(sender: CommandSender, node: String): Boolean {
        val player = (sender as? LuxPlayer)?.parent as? ServerPlayer
        if (player != null) return check(player.createCommandSourceStack(), node, PLAYER_FLOOR_LEVEL)
        if (sender is MinecraftCommandSender) return check(sender.source, node, NON_PLAYER_FLOOR_LEVEL)
        return false
    }

    companion object {
        /** Vanilla op level granted to players when no permission provider answers. */
        const val PLAYER_FLOOR_LEVEL = 4

        /** Vanilla op level granted to console and command blocks when no permission provider answers. */
        const val NON_PLAYER_FLOOR_LEVEL = 2

        /**
         * Checks [node] against [source], falling back to vanilla op level [floorLevel].
         *
         * @param source The command source to check.
         * @param node The permission node.
         * @param floorLevel The op level used when no provider has an opinion.
         * @return true if granted, false otherwise.
         */
        fun check(source: CommandSourceStack, node: String, floorLevel: Int): Boolean {
            return try {
                Permissions.check(source, node, floorLevel)
            } catch (e: LinkageError) {
                source.hasPermission(floorLevel)
            }
        }
    }
}
