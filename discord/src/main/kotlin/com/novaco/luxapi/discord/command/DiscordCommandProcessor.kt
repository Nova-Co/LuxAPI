package com.novaco.luxapi.discord.command

import com.novaco.luxapi.discord.command.annotation.DiscordCommand
import com.novaco.luxapi.discord.command.annotation.DiscordCommandHandler
import com.novaco.luxapi.discord.command.exception.DiscordCommandException
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData
import org.slf4j.LoggerFactory
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method

/**
 * Wraps a single [DiscordCommand]-annotated instance: builds the JDA [SlashCommandData]
 * for registration, and routes an incoming [SlashCommandInteractionEvent] to the
 * instance's [DiscordCommandHandler]-annotated method.
 *
 * @param commandInstance The object instance annotated with `@DiscordCommand`.
 */
class DiscordCommandProcessor(private val commandInstance: Any) {

    private companion object {
        val logger = LoggerFactory.getLogger(DiscordCommandProcessor::class.java)
    }

    val commandInfo: DiscordCommand = commandInstance.javaClass.getAnnotation(DiscordCommand::class.java)
        ?: throw IllegalArgumentException("Class ${commandInstance.javaClass.simpleName} is missing the @DiscordCommand annotation.")

    private val handlerMethod: Method = commandInstance.javaClass.declaredMethods.firstOrNull {
        it.isAnnotationPresent(DiscordCommandHandler::class.java)
    } ?: throw IllegalArgumentException("Class ${commandInstance.javaClass.simpleName} has no @DiscordCommandHandler method.")

    init {
        handlerMethod.isAccessible = true
    }

    /**
     * Builds the JDA slash command definition for this command, ready to be passed to
     * `JDA#updateCommands()` or `Guild#updateCommands()`.
     */
    fun toCommandData(): SlashCommandData = Commands.slash(commandInfo.name, commandInfo.description)

    /**
     * Invokes the handler method for an incoming interaction. Handler exceptions never
     * propagate: a [DiscordCommandException] sends its message as an ephemeral reply;
     * any other exception is logged and a generic ephemeral error is sent instead.
     *
     * @param event The slash command interaction to handle.
     */
    fun handle(event: SlashCommandInteractionEvent) {
        try {
            handlerMethod.invoke(commandInstance, event)
        } catch (e: InvocationTargetException) {
            val cause = e.cause
            if (cause is DiscordCommandException) {
                event.reply(cause.errorMessage).setEphemeral(true).queue()
            } else {
                logger.error("Unhandled exception while executing Discord command '{}'", commandInfo.name, cause)
                event.reply("An internal error occurred while executing this command.").setEphemeral(true).queue()
            }
        } catch (e: IllegalAccessException) {
            logger.error("Unexpected exception while executing Discord command '{}'", commandInfo.name, e)
            event.reply("An internal error occurred while executing this command.").setEphemeral(true).queue()
        } catch (e: IllegalArgumentException) {
            logger.error("Unexpected exception while executing Discord command '{}'", commandInfo.name, e)
            event.reply("An internal error occurred while executing this command.").setEphemeral(true).queue()
        }
    }
}
