package com.novaco.luxapi.neoforge.command

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import com.novaco.luxapi.commons.command.AbstractCommandManager
import com.novaco.luxapi.commons.command.CommandProcessor
import com.novaco.luxapi.neoforge.player.NeoForgeLuxPlayer
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands

class NeoForgeCommandManager : AbstractCommandManager() {

    private lateinit var dispatcher: CommandDispatcher<CommandSourceStack>

    fun setDispatcher(dispatcher: CommandDispatcher<CommandSourceStack>) {
        this.dispatcher = dispatcher
    }

    override fun registerToPlatform(processor: CommandProcessor) {
        val commandName = processor.commandInfo.name.lowercase()
        val commandBuilder = Commands.literal(commandName)
            .executes { context -> executeCommand(context, processor, emptyArray()) }

        val argsBuilder = Commands.argument("args", StringArgumentType.greedyString())
            .executes { context ->
                val rawArgs = StringArgumentType.getString(context, "args")
                val argsArray = rawArgs.split(" ").toTypedArray()
                executeCommand(context, processor, argsArray)
            }

        commandBuilder.then(argsBuilder)
        dispatcher.register(commandBuilder)
    }

    private fun executeCommand(context: CommandContext<CommandSourceStack>, processor: CommandProcessor, args: Array<String>): Int {
        val source = context.source
        val sender = if (source.isPlayer) {
            NeoForgeLuxPlayer(source.playerOrException)
        } else {
            null // Console sender implementation goes here
        }

        if (sender != null) {
            processor.process(sender, args)
        }
        return 1
    }
}