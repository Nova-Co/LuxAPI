package com.novaco.luxapi.discord.user

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel
import net.dv8tion.jda.api.requests.restaction.CacheRestAction
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.mockito.kotlin.mock
import java.util.concurrent.CompletableFuture

class UtilUserTest {

    @Test
    fun `test findById returns the cached user when present`() {
        val jda = mock<JDA>()
        val user = mock<User>()
        `when`(jda.getUserById(42L)).thenReturn(user)

        assertSame(user, UtilUser.findById(jda, 42L))
    }

    @Test
    fun `test findById returns null when the user is not cached`() {
        val jda = mock<JDA>()
        `when`(jda.getUserById(42L)).thenReturn(null)

        assertNull(UtilUser.findById(jda, 42L))
    }

    @Test
    fun `test sendDirectMessage completes normally when the DM succeeds`() {
        val user = mock<User>()
        val channel = mock<PrivateChannel>()
        val openAction = mock<CacheRestAction<PrivateChannel>>()
        val sendAction = mock<MessageCreateAction>()

        `when`(user.openPrivateChannel()).thenReturn(openAction)
        `when`(openAction.submit()).thenReturn(CompletableFuture.completedFuture(channel))
        `when`(channel.sendMessage(anyString())).thenReturn(sendAction)
        `when`(sendAction.submit()).thenReturn(CompletableFuture.completedFuture(mock()))

        val result = UtilUser.sendDirectMessage(user, "hello")

        assertDoesNotThrow { result.get() }
    }

    @Test
    fun `test sendDirectMessage completes exceptionally when the channel cannot be opened`() {
        val user = mock<User>()
        val openAction = mock<CacheRestAction<PrivateChannel>>()
        val failed = CompletableFuture<PrivateChannel>()
        failed.completeExceptionally(RuntimeException("DMs closed"))

        `when`(user.openPrivateChannel()).thenReturn(openAction)
        `when`(openAction.submit()).thenReturn(failed)

        val result = UtilUser.sendDirectMessage(user, "hello")

        assertTrue(result.isCompletedExceptionally)
    }
}
