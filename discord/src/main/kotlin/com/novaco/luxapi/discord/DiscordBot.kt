package com.novaco.luxapi.discord

import com.novaco.luxapi.discord.command.DiscordCommandManager
import com.novaco.luxapi.discord.config.DiscordConfig
import com.novaco.luxapi.discord.event.DiscordEventBridge
import com.novaco.luxapi.discord.listener.InteractionListener
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.exceptions.ErrorResponseException
import net.dv8tion.jda.api.exceptions.InvalidTokenException
import org.slf4j.LoggerFactory

/**
 * Lifecycle wrapper around a JDA client. [start] never throws into the caller's own
 * plugin-enable sequence — a bad token or unreachable Discord degrades to "Discord
 * features disabled", logged, rather than crashing the host plugin.
 *
 * @param config Bot connection settings (token, command-registration guild).
 * @param commandManager The command manager whose registered commands get pushed to
 *   Discord once the connection is ready.
 */
class DiscordBot(private val config: DiscordConfig, private val commandManager: DiscordCommandManager) {

    private companion object {
        val logger = LoggerFactory.getLogger(DiscordBot::class.java)
    }

    private var jda: JDA? = null

    /**
     * Connects to Discord and blocks until the connection is ready, then pushes
     * [commandManager]'s registered slash commands.
     *
     * @param jdaBuilderFactory Builds and connects the JDA client. Defaults to a real
     *   [JDABuilder] using [config]'s token and this bot's listeners — overridable for
     *   testing the Result-wrapping behavior without a real network connection.
     * @return `Result.success` with the connected [JDA] instance, or `Result.failure`
     *   with whatever [jdaBuilderFactory] threw.
     */
    fun start(jdaBuilderFactory: () -> JDA = { buildDefaultJda() }): Result<JDA> {
        return try {
            val connected = jdaBuilderFactory()
            jda = connected
            commandManager.updateCommands(connected, config.guildId)
            Result.success(connected)
        } catch (e: InvalidTokenException) {
            logger.error("Discord bot login failed: Invalid or missing bot token", e)
            Result.failure(e)
        } catch (e: InterruptedException) {
            Thread.currentThread().interrupt() // คืนสถานะ interrupted flag ให้ thread
            logger.error("Discord bot startup was interrupted while waiting for READY state", e)
            Result.failure(e)
        } catch (e: ErrorResponseException) {
            logger.error("Discord REST error occurred during command registration (Error code: ${e.errorCode})", e)
            Result.failure(e)
        } catch (e: IllegalArgumentException) {
            logger.error("Invalid Discord configuration parameter (Token, Guild ID, or Intents)", e)
            Result.failure(e)
        } catch (e: IllegalStateException) {
            logger.error("Discord bot entered an illegal state during startup", e)
            Result.failure(e)
        }
    }

    private fun buildDefaultJda(): JDA {
        return JDABuilder.createDefault(config.token)
            .addEventListeners(DiscordEventBridge(), InteractionListener(commandManager))
            .build()
            .awaitReady()
    }

    /** Shuts down the JDA client. Safe to call even if [start] was never called. */
    fun shutdown() {
        jda?.shutdown()
    }
}
