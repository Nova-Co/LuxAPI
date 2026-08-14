package com.novaco.luxapi.discord.user

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.User
import java.util.concurrent.CompletableFuture

/**
 * Discord user lookup and messaging helpers over raw JDA types. No persistence, no
 * fixed identity mapping — pairing a [User] with anything app-specific (an MC player,
 * a database row) is a consumer-plugin concern.
 */
object UtilUser {

    /**
     * Looks up a user by id from JDA's in-memory cache. Returns `null` if the user
     * isn't cached (this does not perform a REST lookup) — a bot generally only has
     * users it shares a guild or a channel with in cache.
     */
    fun findById(jda: JDA, discordId: Long): User? {
        return jda.getUserById(discordId)
    }

    /**
     * Sends [content] to [user] as a direct message. The returned future completes
     * exceptionally instead of throwing synchronously if the channel can't be opened
     * (e.g. the user has DMs closed or has blocked the bot) — an expected outcome the
     * caller decides how to handle, not a bug.
     */
    fun sendDirectMessage(user: User, content: String): CompletableFuture<Unit> {
        return user.openPrivateChannel().submit()
            .thenCompose { channel -> channel.sendMessage(content).submit() }
            .thenApply { }
    }
}
