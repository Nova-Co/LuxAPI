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
     *
     * @param processor The processed command container metadata.
     * @param targetDispatcher The live platform command router.
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

                    if (rawArgsString.isEmpty()) return@suggests builder.buildFuture()

                    val fullArgs = rawArgsString.split(" ").toTypedArray()
                    val adjustedArgs = if (rawArgsString.endsWith(" ")) fullArgs + "" else fullArgs
                    val methodArgs = if (adjustedArgs.size > 1) adjustedArgs.drop(1).toTypedArray() else emptyArray()

                    val source = context.source
                    if (source.isPlayer) {
                        val sender = NeoForgeLuxPlayer(source.playerOrException)
                        val currentWord = if (rawArgsString.endsWith(" ")) "" else adjustedArgs.lastOrNull() ?: ""

                        processor.getSuggestions(sender, methodArgs)
                            .filter { it.lowercase().startsWith(currentWord.lowercase()) }
                            .forEach { builder.suggest(it) }
                    }

                    builder.buildFuture()
                }
                .executes { context ->
                    val rawArgs = StringArgumentType.getString(context, "args")
                    val fullArgsArray = rawArgs.split(" ").filter { it.isNotEmpty() }.toTypedArray()
                    val methodArgsArray = if (fullArgsArray.size > 1) fullArgsArray.drop(1).toTypedArray() else emptyArray()

                    executeCommand(context, processor, methodArgsArray)
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
        } else null

        if (sender != null) {
            processor.process(sender, args)
        }
        return 1
    }
}