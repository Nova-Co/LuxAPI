package com.novaco.luxapi.commons.permission

import com.novaco.luxapi.commons.command.sender.CommandSender

/**
 * Platform-supplied permission backend, registered once at startup through
 * [com.novaco.luxapi.commons.service.ServiceManager] and consumed via [LuxPermissions].
 *
 * Implementations resolve a node in this order: the installed permission provider if it
 * has an opinion, otherwise the platform-native check, otherwise a vanilla op-level floor.
 * A provider that explicitly denies a node therefore denies it even for operators.
 */
interface PermissionChecker {

    /**
     * Resolves whether [sender] holds [node].
     *
     * @param sender The player, console, or command block asking.
     * @param node The permission node, e.g. `luxapi.command.admin`.
     * @return true if access is granted, false otherwise.
     */
    fun has(sender: CommandSender, node: String): Boolean

    /**
     * Announces a node that will be checked later, so backends that require up-front
     * registration can include it. The default does nothing.
     *
     * @param node The permission node being declared.
     */
    fun declare(node: String) {}
}
