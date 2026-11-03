package com.novaco.luxapi.commons.permission

import com.novaco.luxapi.commons.command.sender.CommandSender
import com.novaco.luxapi.commons.service.ServiceManager
import org.slf4j.LoggerFactory

/**
 * Sender-independent entry point for permission checks. Works for players, console and
 * command blocks alike by delegating to the [PermissionChecker] the platform registered.
 *
 * All paths fail closed: a blank node, a missing checker with no fallback, or a checker
 * that throws all resolve to false.
 */
object LuxPermissions {

    private val logger = LoggerFactory.getLogger(LuxPermissions::class.java)

    /**
     * Checks whether [sender] holds [node].
     *
     * @param sender The sender to check.
     * @param node The permission node. Blank nodes are always denied.
     * @param fallback Evaluated only when no [PermissionChecker] is registered. Defaults to deny.
     * @return true if access is granted, false otherwise.
     */
    fun has(sender: CommandSender, node: String, fallback: () -> Boolean = { false }): Boolean {
        if (node.isBlank()) return false
        val checker = ServiceManager.get(PermissionChecker::class.java) ?: return fallback()
        return try {
            checker.has(sender, node)
        } catch (e: Exception) {
            logger.error("Permission check for node '{}' failed; denying.", node, e)
            false
        }
    }

    /**
     * Announces a node to the registered [PermissionChecker] so backends that need up-front
     * registration can include it. Does nothing when no checker is registered or the node is blank.
     *
     * @param node The permission node that will be checked later.
     */
    fun declare(node: String) {
        if (node.isBlank()) return
        ServiceManager.get(PermissionChecker::class.java)?.declare(node)
    }
}
