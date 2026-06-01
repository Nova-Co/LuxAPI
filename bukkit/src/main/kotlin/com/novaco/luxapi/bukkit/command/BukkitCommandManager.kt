package com.novaco.luxapi.bukkit.command

import com.novaco.luxapi.commons.command.CommandManager
import com.novaco.luxapi.commons.command.CommandProcessor
import com.novaco.luxapi.commons.command.annotation.Command
import org.bukkit.Bukkit
import org.bukkit.command.SimpleCommandMap
import org.bukkit.plugin.Plugin
import java.lang.reflect.Field

/**
 * Bukkit-specific implementation of the CommandManager.
 * Utilizes reflection to inject commands dynamically into the server's CommandMap.
 */
class BukkitCommandManager(private val plugin: Plugin) : CommandManager {

    private val commandMap: SimpleCommandMap

    init {
        commandMap = getBukkitCommandMap()
    }

    /**
     * Extracts the SimpleCommandMap from the Bukkit server using reflection.
     */
    private fun getBukkitCommandMap(): SimpleCommandMap {
        try {
            val server = Bukkit.getServer()
            val commandMapField: Field = server.javaClass.getDeclaredField("commandMap")
            commandMapField.isAccessible = true
            return commandMapField.get(server) as SimpleCommandMap
        } catch (e: Exception) {
            throw RuntimeException("Failed to extract Bukkit CommandMap via reflection", e)
        }
    }

    /**
     * Registers an annotated command instance directly into the Bukkit runtime.
     */
    override fun register(commandInstance: Any) {
        val clazz = commandInstance.javaClass
        if (!clazz.isAnnotationPresent(Command::class.java)) {
            throw IllegalArgumentException("Command instance must be annotated with @Command")
        }

        val annotation = clazz.getAnnotation(Command::class.java)
        val processor = CommandProcessor(commandInstance)

        val bukkitCommand = BukkitCommandWrapper(
            name = annotation.name,
            description = annotation.description,
            usageMessage = "/${annotation.name}",
            aliases = annotation.aliases.toList(),
            processor = processor
        )

        // Inject the command using the plugin's name as the fallback prefix
        commandMap.register(plugin.name.lowercase(), bukkitCommand)
    }
}