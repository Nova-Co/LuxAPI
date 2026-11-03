package com.novaco.luxapi.bukkit.permission

import com.novaco.luxapi.commons.command.sender.CommandSender
import com.novaco.luxapi.commons.permission.PermissionChecker

/**
 * Bukkit [PermissionChecker] that defers to the server's native permission system.
 *
 * Bukkit senders already resolve nodes through `hasPermission`, which honours any installed
 * permission plugin, so this simply asks the sender and lets [com.novaco.luxapi.commons.permission.LuxPermissions]
 * expose the same entry point as the other platforms.
 */
class BukkitPermissionChecker : PermissionChecker {

    /**
     * Asks the sender's native permission system whether it holds [node].
     *
     * @param sender A Bukkit-backed [CommandSender] or [com.novaco.luxapi.commons.player.LuxPlayer].
     * @param node The permission node to check.
     * @return true if the sender holds the node, false otherwise.
     */
    override fun has(sender: CommandSender, node: String): Boolean = sender.hasPermission(node)
}
