package com.novaco.luxapi.discord.config

import com.novaco.luxapi.commons.config.LuxConfig
import com.novaco.luxapi.commons.config.annotation.Comment
import com.novaco.luxapi.commons.config.annotation.Config

/**
 * Configuration file for the Discord bot connection.
 * Automatically generated and managed by the LuxAPI ConfigService.
 */
@Config("discord.yml")
@Comment("Configure your Discord bot connection here.")
class DiscordConfig : LuxConfig() {

    @Comment("The bot token from the Discord Developer Portal. Keep this secret.")
    var token: String = ""

    @Comment("Guild ID to register slash commands to for fast, guild-scoped updates during development. Set to 0 to register commands globally instead (can take up to an hour to propagate).")
    var guildId: Long = 0L
}
