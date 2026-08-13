package com.novaco.luxapi.cobblemon.permission

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.permission.CobblemonPermissions
import com.cobblemon.mod.common.api.permission.Permission
import net.minecraft.server.level.ServerPlayer

/**
 * Query wrapper around Cobblemon's own internal permission-node system
 * ([Cobblemon.permissionValidator]) — distinct from LuxAPI's own command-permission
 * gate in `commons/command/CommandProcessor.kt` (unrelated, already covered). This
 * wraps checks against Cobblemon's own permission nodes (its `/cobblemon` commands, PC
 * access, etc.), useful when a plugin dev wants to gate a custom command/action behind
 * the same permission a player would need for the equivalent vanilla Cobblemon
 * feature.
 *
 * **Scope note:** [CobblemonPermissions] is a fixed, read-only list of nodes (its own
 * `create` is `private`) — there's no runtime registration of new Cobblemon
 * permission nodes here. A caller gating a custom node that isn't one of
 * [CobblemonPermissions]' own predefined ones should use the raw string+level
 * [hasPermission] overload instead.
 */
object PermissionManager {

    fun allNodes(): Iterable<Permission> = CobblemonPermissions.all()

    /**
     * Checks whether [player] has [permission], delegating to whatever
     * [com.cobblemon.mod.common.api.permission.PermissionValidator] is currently
     * installed at [Cobblemon.permissionValidator] (vanilla op-level by default, or a
     * platform permission plugin if the server replaced it).
     */
    fun hasPermission(player: ServerPlayer, permission: Permission): Boolean =
        Cobblemon.permissionValidator.hasPermission(player, permission)

    /**
     * Checks [player] against a raw permission node string and vanilla op [level]
     * (0-4), via Cobblemon's own [Cobblemon.permissionValidator] — useful for gating a
     * custom node that isn't one of [CobblemonPermissions]' own predefined ones.
     */
    fun hasPermission(player: ServerPlayer, node: String, level: Int): Boolean =
        Cobblemon.permissionValidator.hasPermission(player, node, level)
}
