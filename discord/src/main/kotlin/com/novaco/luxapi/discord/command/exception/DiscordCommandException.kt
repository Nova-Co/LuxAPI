package com.novaco.luxapi.discord.command.exception

/**
 * Thrown by a [com.novaco.luxapi.discord.command.annotation.DiscordCommandHandler]
 * method to send a specific, user-facing ephemeral error reply instead of the
 * generic internal-error message.
 *
 * @param errorMessage The user-friendly error message that will be sent as an
 *   ephemeral reply to the invoking user.
 */
class DiscordCommandException(val errorMessage: String) : RuntimeException(errorMessage)
