package com.novaco.luxapi.discord

import com.novaco.luxapi.discord.command.DiscordCommandManager
import com.novaco.luxapi.discord.config.DiscordConfig
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class DiscordBotTest {

    @Test
    fun `test start returns a failed Result when the jda builder factory throws`() {
        val bot = DiscordBot(DiscordConfig(), DiscordCommandManager())
        val failure = IllegalStateException("bad token")

        val result = bot.start { throw failure }

        assertTrue(result.isFailure)
        assertSame(failure, result.exceptionOrNull())
    }

    @Test
    fun `test shutdown does nothing when the bot was never started`() {
        val bot = DiscordBot(DiscordConfig(), DiscordCommandManager())

        assertDoesNotThrow { bot.shutdown() }
    }
}
