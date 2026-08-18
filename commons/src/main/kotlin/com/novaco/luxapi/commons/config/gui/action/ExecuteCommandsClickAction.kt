package com.novaco.luxapi.commons.config.gui.action

import com.novaco.luxapi.commons.chat.placeholder.PlaceholderManager
import com.novaco.luxapi.commons.config.gui.ClickAction
import com.novaco.luxapi.commons.config.gui.ConfigGuiRegistries
import com.novaco.luxapi.commons.gui.GuiClickEvent
import org.spongepowered.configurate.objectmapping.ConfigSerializable

/**
 * Runs a list of commands for the clicking player. Registered under id `"commands"`.
 *
 * Commons has no cross-platform "run this command string" hook of its own — only platform code
 * (Bukkit/Fabric/NeoForge) can actually dispatch one. A platform bootstrap must set
 * [ConfigGuiRegistries.commandDispatcher] for this action to do anything; until then it's a
 * documented no-op rather than a crash.
 */
@ConfigSerializable
class ExecuteCommandsClickAction : ClickAction {

    var commands: MutableList<String> = mutableListOf()

    override fun id(): String = "commands"

    override fun handle(event: GuiClickEvent): Boolean {
        val dispatcher = ConfigGuiRegistries.commandDispatcher ?: return true
        commands.forEach { command ->
            dispatcher.dispatch(event.player, PlaceholderManager.replace(event.player, command))
        }
        return true
    }
}
