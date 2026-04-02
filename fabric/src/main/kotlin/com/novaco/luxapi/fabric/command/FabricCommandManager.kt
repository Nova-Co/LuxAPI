package com.novaco.luxapi.fabric.command

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import com.novaco.luxapi.commons.command.AbstractCommandManager
import com.novaco.luxapi.commons.command.CommandProcessor
import com.novaco.luxapi.fabric.player.FabricLuxPlayer
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.commands.SharedSuggestionProvider
import java.util.concurrent.CompletableFuture

/**
 * Manages Fabric-specific command registration and bridges Brigadier with LuxAPI logic.
 */
class FabricCommandManager : AbstractCommandManager() {

    private var dispatcher: CommandDispatcher<CommandSourceStack>? = null
    private val commandCache = mutableListOf<CommandProcessor>()

    /**
     * Initializes the command dispatcher and registers all cached commands.
     */
    fun setDispatcher(dispatcher: CommandDispatcher<CommandSourceStack>) {
        this.dispatcher = dispatcher
        commandCache.forEach { registerNode(it, dispatcher) }
    }

    /**
     * Stores the command processor and registers it to the platform if the dispatcher is ready.
     */
    override fun registerToPlatform(processor: CommandProcessor) {
        commandCache.add(processor)
        dispatcher?.let { registerNode(processor, it) }
    }

    /**
     * Registers the command as a root literal followed by a dynamic argument to handle all inputs.
     */
    private fun registerNode(processor: CommandProcessor, targetDispatcher: CommandDispatcher<CommandSourceStack>) {
        val commandName = processor.commandInfo.name.lowercase()

        val rootNode = Commands.literal(commandName)
            .executes { context -> executeCommand(context, processor, emptyArray()) }

        val argumentNode = Commands.argument("args", StringArgumentType.greedyString())
            .suggests { context, builder ->
                val remainingInput = builder.remaining.lowercase()
                val args = if (remainingInput.isEmpty()) emptyArray() else remainingInput.split(" ").toTypedArray()

                val source = context.source
                if (source.isPlayer) {
                    val sender = FabricLuxPlayer(source.playerOrException)
                    val onlinePlayers = source.server.playerList.players.map { it.scoreboardName }

                    processor.getSuggestions(sender, args, onlinePlayers).forEach { builder.suggest(it) }
                }

                builder.buildFuture()
            }
            .executes { context ->
                val rawArgs = StringArgumentType.getString(context, "args")
                val argsArray = rawArgs.split(" ").filter { it.isNotEmpty() }.toTypedArray()
                executeCommand(context, processor, argsArray)
            }

        rootNode.then(argumentNode)
        targetDispatcher.register(rootNode)
    }

    /**
     * Provides tab suggestions for player names from the current server instance.
     */
    private fun suggestArguments(
        context: CommandContext<CommandSourceStack>,
        builder: SuggestionsBuilder
    ): CompletableFuture<Suggestions> {
        val players = context.source.server.playerList.players.map { it.scoreboardName }
        return SharedSuggestionProvider.suggest(players, builder)
    }

    /**
     * Executes the command logic by wrapping the Minecraft source as a LuxPlayer.
     */
    private fun executeCommand(
        context: CommandContext<CommandSourceStack>,
        processor: CommandProcessor,
        args: Array<String>
    ): Int {
        val source = context.source
        val sender = if (source.isPlayer) {
            FabricLuxPlayer(source.playerOrException)
        } else null

        if (sender != null) {
            processor.process(sender, args)
        }
        return 1
    }
}