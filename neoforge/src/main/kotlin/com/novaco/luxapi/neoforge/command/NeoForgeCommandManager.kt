package com.novaco.luxapi.neoforge.command

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import com.novaco.luxapi.commons.command.AbstractCommandManager
import com.novaco.luxapi.commons.command.CommandProcessor
import com.novaco.luxapi.core.command.MinecraftCommandSender
import com.novaco.luxapi.neoforge.player.NeoForgeLuxPlayer
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands

/**
 * Bridges Brigadier registries with the core LuxAPI annotation-driven CommandProcessor on the NeoForge platform.
 */
class NeoForgeCommandManager : AbstractCommandManager() {

    private var dispatcher: CommandDispatcher<CommandSourceStack>? = null
    private val commandCache = mutableListOf<CommandProcessor>()

    /**
     * Assigns the main Brigadier server dispatcher and flushes all cached command setups.
     *
     * @param dispatcher The target CommandDispatcher node stack.
     */
    fun setDispatcher(dispatcher: CommandDispatcher<CommandSourceStack>) {
        this.dispatcher = dispatcher
        commandCache.forEach { registerNode(it, dispatcher) }
    }

    override fun registerToPlatform(processor: CommandProcessor) {
        commandCache.add(processor)
        dispatcher?.let { registerNode(processor, it) }
    }

    /**
     * Translates a command processor layout into standard native greedy string literal configurations.
     */
    private fun registerNode(processor: CommandProcessor, targetDispatcher: CommandDispatcher<CommandSourceStack>) {
        val commandNames = mutableListOf(processor.commandInfo.name.lowercase())
        commandNames.addAll(processor.commandInfo.aliases.map { it.lowercase() })

        for (commandName in commandNames) {
            val rootNode = Commands.literal(commandName)
                .executes { context -> executeCommand(context, processor, emptyArray()) }

            val argumentNode = Commands.argument("args", StringArgumentType.greedyString())
                .suggests { context, builder ->
                    val fullInput = builder.input
                    val commandPrefix = "/$commandName "

                    val rawArgsString = if (fullInput.startsWith(commandPrefix, ignoreCase = true)) {
                        fullInput.substring(commandPrefix.length)
                    } else {
                        builder.remaining
                    }

                    // Keeping the exact split to ensure trailing spaces generate an empty string
                    val argsList = rawArgsString.split(" ").toTypedArray()
                    val currentWord = argsList.lastOrNull() ?: ""

                    // Calculate the proper offset to prevent overwriting previous arguments
                    val offset = fullInput.length - currentWord.length
                    val offsetBuilder = builder.createOffset(offset)

                    val source = context.source
                    if (source.isPlayer) {
                        val sender = NeoForgeLuxPlayer(source.playerOrException)

                        processor.getSuggestions(sender, argsList)
                            .filter { it.lowercase().startsWith(currentWord.lowercase()) }
                            .forEach { offsetBuilder.suggest(it) }
                    }

                    offsetBuilder.buildFuture()
                }
                .executes { context ->
                    val rawArgs = StringArgumentType.getString(context, "args")
                    val fullArgsArray = rawArgs.split(" ").filter { it.isNotEmpty() }.toTypedArray()

                    executeCommand(context, processor, fullArgsArray)
                }

            rootNode.then(argumentNode)
            targetDispatcher.register(rootNode)
        }
    }

    /**
     * Invokes the processor pipeline by wrapping raw Brigadier command interactions.
     *
     * @param context The current execution snapshot context.
     * @param processor The associated command layout blueprint.
     * @param args The adjusted method execution arguments.
     * @return Execution confirmation value.
     */
    private fun executeCommand(
        context: CommandContext<CommandSourceStack>,
        processor: CommandProcessor,
        args: Array<String>
    ): Int {
        val source = context.source
        val sender = if (source.isPlayer) {
            NeoForgeLuxPlayer(source.playerOrException)
        } else {
            MinecraftCommandSender(source)
        }

        processor.process(sender, args)
        return 1
    }
}