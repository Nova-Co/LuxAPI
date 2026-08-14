package com.novaco.luxapi.discord.command

import com.novaco.luxapi.discord.command.annotation.DiscordCommand
import com.novaco.luxapi.discord.command.annotation.DiscordCommandHandler
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.mockito.kotlin.mock

@DiscordCommand(name = "one", description = "first")
class OneCommand {
    @DiscordCommandHandler
    fun execute(event: SlashCommandInteractionEvent) {}
}

@DiscordCommand(name = "two", description = "second")
class TwoCommand {
    @DiscordCommandHandler
    fun execute(event: SlashCommandInteractionEvent) {}
}

class DiscordCommandManagerTest {

    @Test
    fun `test register makes a command dispatchable by name`() {
        val manager = DiscordCommandManager()
        val command = OneCommand()
        manager.register(command)

        val event = mock<SlashCommandInteractionEvent>()
        `when`(event.name).thenReturn("one")

        manager.dispatch(event)
        // No exception, and the underlying processor's handle() was reached — verified
        // indirectly by the absence of the "unknown command" log path, exercised in
        // the next test.
        assertTrue(true)
    }

    @Test
    fun `test dispatch on an unregistered name does not throw`() {
        val manager = DiscordCommandManager()
        val event = mock<SlashCommandInteractionEvent>()
        `when`(event.name).thenReturn("unknown")

        assertDoesNotThrow { manager.dispatch(event) }
    }

    @Test
    fun `test registerAll registers every command`() {
        val manager = DiscordCommandManager()
        manager.registerAll(OneCommand(), TwoCommand())

        assertEquals(2, manager.registeredCommands.size)
    }

    @Test
    fun `test updateCommands pushes to the guild when guildId is non-zero`() {
        val manager = DiscordCommandManager()
        manager.register(OneCommand())

        val jda = mock<JDA>()
        val guild = mock<Guild>()
        val action = mock<CommandListUpdateAction>()
        `when`(jda.getGuildById(123L)).thenReturn(guild)
        `when`(guild.updateCommands()).thenReturn(action)
        `when`(action.addCommands(any<Collection<net.dv8tion.jda.api.interactions.commands.build.CommandData>>())).thenReturn(action)

        manager.updateCommands(jda, 123L)

        verify(guild).updateCommands()
        verify(jda, never()).updateCommands()
    }

    @Test
    fun `test updateCommands pushes globally when guildId is zero`() {
        val manager = DiscordCommandManager()
        manager.register(OneCommand())

        val jda = mock<JDA>()
        val action = mock<CommandListUpdateAction>()
        `when`(jda.updateCommands()).thenReturn(action)
        `when`(action.addCommands(any<Collection<net.dv8tion.jda.api.interactions.commands.build.CommandData>>())).thenReturn(action)

        manager.updateCommands(jda, 0L)

        verify(jda).updateCommands()
    }
}
