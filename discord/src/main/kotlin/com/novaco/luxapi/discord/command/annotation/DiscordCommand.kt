package com.novaco.luxapi.discord.command.annotation

/**
 * Marks a class as a registrable Discord slash command.
 *
 * @property name The slash command's name, as it will appear in Discord.
 * @property description The slash command's description, as it will appear in Discord.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class DiscordCommand(
    val name: String,
    val description: String
)
