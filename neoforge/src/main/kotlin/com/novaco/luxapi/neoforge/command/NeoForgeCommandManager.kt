package com.novaco.luxapi.neoforge.command

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import com.novaco.luxapi.commons.command.AbstractCommandManager
import com.novaco.luxapi.commons.command.CommandProcessor
import com.novaco.luxapi.neoforge.player.NeoForgeLuxPlayer
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands

/**
 * Manages NeoForge-specific command registration and bridges Brigadier with LuxAPI logic.
 * This class uses a caching mechanism to ensure commands are safely registered.
 */
class NeoForgeCommandManager : AbstractCommandManager() {

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
     * Stores the command processor in the cache and registers it to the platform
     * immediately if the dispatcher is already available.
     */
    override fun registerToPlatform(processor: CommandProcessor) {
        commandCache.add(processor)
        dispatcher?.let { registerNode(processor, it) }
    }

    /**
     * Registers the command as a root literal followed by a dynamic argument node.
     * This structure delegates all string parsing and tab completion logic to the core processor.
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
                    val sender = NeoForgeLuxPlayer(source.playerOrException)
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
     * Executes the command logic by wrapping the Minecraft source as a LuxPlayer.
     */
    private fun executeCommand(
        context: CommandContext<CommandSourceStack>,
        processor: CommandProcessor,
        args: Array<String>
    ): Int {
        val source = context.source
        val sender = if (source.isPlayer) {
            NeoForgeLuxPlayer(source.playerOrException)
        } else null

        if (sender != null) {
            processor.process(sender, args)
        }
        return 1
    }
}