package com.novaco.luxapi.discord.command.annotation

/**
 * Marks the method on a [DiscordCommand]-annotated class that handles the slash
 * command interaction. The method must accept exactly one parameter of type
 * [net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent].
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class DiscordCommandHandler
