package com.novaco.luxapi.discord.permission

import net.dv8tion.jda.api.entities.Member
import org.slf4j.LoggerFactory

/**
 * Role/permission check and mutation helpers over a raw JDA [Member]. No fixed
 * role-to-anything mapping — the caller decides which role id means what.
 */
object UtilRole {

    private val logger = LoggerFactory.getLogger(UtilRole::class.java)

    /** Returns whether [member] currently has a role with id [roleId]. */
    fun hasRole(member: Member, roleId: Long): Boolean {
        return member.roles.any { it.idLong == roleId }
    }

    /**
     * Grants the role with id [roleId] to [member]. Fire-and-forget: logs a warning
     * and does nothing if the role isn't visible to this bot, logs an error if the
     * REST call itself fails.
     */
    fun grant(member: Member, roleId: Long) {
        val guild = member.guild
        val role = guild.getRoleById(roleId)
        if (role == null) {
            logger.warn("Cannot grant role '{}': not visible in guild '{}'", roleId, guild.id)
            return
        }
        guild.addRoleToMember(member, role).queue(
            {},
            { throwable -> logger.error("Failed to grant role '{}' to member '{}'", roleId, member.id, throwable) }
        )
    }

    /**
     * Revokes the role with id [roleId] from [member]. Same fire-and-forget shape as
     * [grant].
     */
    fun revoke(member: Member, roleId: Long) {
        val guild = member.guild
        val role = guild.getRoleById(roleId)
        if (role == null) {
            logger.warn("Cannot revoke role '{}': not visible in guild '{}'", roleId, guild.id)
            return
        }
        guild.removeRoleFromMember(member, role).queue(
            {},
            { throwable -> logger.error("Failed to revoke role '{}' from member '{}'", roleId, member.id, throwable) }
        )
    }
}
