package com.novaco.luxapi.discord.command

import com.novaco.luxapi.discord.command.annotation.DiscordCommand
import com.novaco.luxapi.discord.command.annotation.DiscordCommandHandler
import com.novaco.luxapi.discord.command.exception.DiscordCommandException
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.mockito.kotlin.mock

@DiscordCommand(name = "ping", description = "Replies with pong")
class SuccessCommand {
    var invoked = false

    @DiscordCommandHandler
    fun execute(event: SlashCommandInteractionEvent) {
        invoked = true
    }
}

@DiscordCommand(name = "fail-controlled", description = "Always fails with a controlled message")
class ControlledFailureCommand {
    @DiscordCommandHandler
    fun execute(event: SlashCommandInteractionEvent) {
        throw DiscordCommandException("You can't do that.")
    }
}

@DiscordCommand(name = "fail-unexpected", description = "Always fails unexpectedly")
class UnexpectedFailureCommand {
    @DiscordCommandHandler
    fun execute(event: SlashCommandInteractionEvent) {
        throw IllegalStateException("boom")
    }
}

class DiscordCommandProcessorTest {

    private fun mockEvent(): SlashCommandInteractionEvent {
        val event = mock<SlashCommandInteractionEvent>()
        val replyAction = mock<ReplyCallbackAction>()
        `when`(event.reply(anyString())).thenReturn(replyAction)
        `when`(replyAction.setEphemeral(anyBoolean())).thenReturn(replyAction)
        return event
    }

    @Test
    fun `test commandInfo is extracted from the annotation`() {
        val processor = DiscordCommandProcessor(SuccessCommand())

        assertEquals("ping", processor.commandInfo.name)
        assertEquals("Replies with pong", processor.commandInfo.description)
    }

    @Test
    fun `test toCommandData produces matching slash command data`() {
        val processor = DiscordCommandProcessor(SuccessCommand())
        val data = processor.toCommandData()

        assertEquals("ping", data.name)
        assertEquals("Replies with pong", data.description)
    }

    @Test
    fun `test handle invokes the annotated handler method`() {
        val command = SuccessCommand()
        val processor = DiscordCommandProcessor(command)

        processor.handle(mockEvent())

        assertTrue(command.invoked)
    }

    @Test
    fun `test handle replies ephemeral with the controlled message on DiscordCommandException`() {
        val processor = DiscordCommandProcessor(ControlledFailureCommand())
        val event = mockEvent()

        processor.handle(event)

        verify(event).reply("You can't do that.")
    }

    @Test
    fun `test handle replies ephemeral with a generic message on unexpected exceptions`() {
        val processor = DiscordCommandProcessor(UnexpectedFailureCommand())
        val event = mockEvent()

        processor.handle(event)

        verify(event).reply("An internal error occurred while executing this command.")
    }

    @Test
    fun `test constructor throws when the class is missing DiscordCommand`() {
        class NotAnnotated

        assertThrows(IllegalArgumentException::class.java) {
            DiscordCommandProcessor(NotAnnotated())
        }
    }
}
