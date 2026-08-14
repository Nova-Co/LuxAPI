package com.novaco.luxapi.discord.config

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class DiscordConfigTest {

    @Test
    fun `test default discord config values`() {
        val config = DiscordConfig()

        assertEquals("", config.token, "Default token should be empty until an operator sets it.")
        assertEquals(0L, config.guildId, "Default guildId of 0 means global slash-command registration.")
    }

    @Test
    fun `test config values are mutable`() {
        val config = DiscordConfig()
        config.token = "test-token"
        config.guildId = 123456789L

        assertEquals("test-token", config.token)
        assertEquals(123456789L, config.guildId)
    }
}
