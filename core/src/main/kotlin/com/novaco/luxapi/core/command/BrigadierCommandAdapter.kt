package com.novaco.luxapi.core.command

import com.mojang.brigadier.Command
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.StringArgumentType
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands

/**
 * A bridge adapter that connects Minecraft's native Brigadier command dispatcher
 * with the cross-platform LuxAPI CommandProcessor.
 * It utilizes a greedy string capture to intercept all arguments dynamically,
 * delegating the actual parsing logic to the core framework.
 */
object BrigadierCommandAdapter {

    /**
     * Registers a root command into the Minecraft CommandDispatcher.
     *
     * @param dispatcher The native Minecraft command dispatcher instance.
     * @param commandName The root trigger word for the command (e.g., "heal").
     * @param permission An optional permission node to restrict execution at the base level.
     * @param executeHandler A callback that routes the sender and arguments to the core processor.
     */
    fun register(
        dispatcher: CommandDispatcher<CommandSourceStack>,
        commandName: String,
        permission: String?,
        executeHandler: (MinecraftCommandSender, Array<String>) -> Unit
    ) {
        val baseNode = Commands.literal(commandName)

        if (!permission.isNullOrBlank()) {
            baseNode.requires { source ->
                val sender = MinecraftCommandSender(source)
                sender.hasPermission(permission)
            }
        }

        baseNode.executes { context ->
            val sender = MinecraftCommandSender(context.source)
            executeHandler(sender, emptyArray())
            Command.SINGLE_SUCCESS
        }

        val argsNode = Commands.argument("args", StringArgumentType.greedyString())
            .executes { context ->
                val sender = MinecraftCommandSender(context.source)
                val rawArgs = StringArgumentType.getString(context, "args")
                val argsArray = rawArgs.trim().split("\\s+".toRegex()).toTypedArray()

                executeHandler(sender, argsArray)
                Command.SINGLE_SUCCESS
            }

        baseNode.then(argsNode)
        dispatcher.register(baseNode)
    }
}