package com.novaco.luxapi.discord.permission

import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.Role
import net.dv8tion.jda.api.requests.restaction.AuditableRestAction
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.mockito.kotlin.mock

class UtilRoleTest {

    @Test
    fun `test hasRole is true when the member has a role with that id`() {
        val role = mock<Role>()
        `when`(role.idLong).thenReturn(42L)
        val member = mock<Member>()
        `when`(member.roles).thenReturn(listOf(role))

        assertTrue(UtilRole.hasRole(member, 42L))
    }

    @Test
    fun `test hasRole is false when the member lacks that role`() {
        val member = mock<Member>()
        `when`(member.roles).thenReturn(emptyList())

        assertFalse(UtilRole.hasRole(member, 42L))
    }

    @Test
    fun `test grant adds the role to the member when the role exists`() {
        val guild = mock<Guild>()
        val role = mock<Role>()
        val member = mock<Member>()
        `when`(member.guild).thenReturn(guild)
        `when`(guild.getRoleById(42L)).thenReturn(role)
        val action = mock<AuditableRestAction<Void>>()
        `when`(guild.addRoleToMember(member, role)).thenReturn(action)

        UtilRole.grant(member, 42L)

        verify(action).queue(any(), any())
    }

    @Test
    fun `test grant does nothing and does not throw when the role does not exist`() {
        val guild = mock<Guild>()
        val member = mock<Member>()
        `when`(member.guild).thenReturn(guild)
        `when`(guild.getRoleById(42L)).thenReturn(null)

        assertDoesNotThrow { UtilRole.grant(member, 42L) }
        verify(guild, never()).addRoleToMember(any<Member>(), any<Role>())
    }

    @Test
    fun `test revoke removes the role from the member when the role exists`() {
        val guild = mock<Guild>()
        val role = mock<Role>()
        val member = mock<Member>()
        `when`(member.guild).thenReturn(guild)
        `when`(guild.getRoleById(42L)).thenReturn(role)
        val action = mock<AuditableRestAction<Void>>()
        `when`(guild.removeRoleFromMember(member, role)).thenReturn(action)

        UtilRole.revoke(member, 42L)

        verify(action).queue(any(), any())
    }
}
