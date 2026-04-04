package com.novaco.luxapi.core.chat

import com.novaco.luxapi.core.text.TextUtils
import net.minecraft.network.chat.ClickEvent
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.HoverEvent
import net.minecraft.network.chat.MutableComponent
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.item.ItemStack

/**
 * A fluent builder for creating highly interactive chat messages.
 * Supports appending text with hover tooltips, item previews, and clickable actions.
 */
class SmartMessage {

    private val rootComponent: MutableComponent = Component.empty()

    /**
     * Appends standard text to the message.
     * Automatically translates legacy color codes.
     *
     * @param text The raw string with color codes.
     */
    fun append(text: String): SmartMessage {
        rootComponent.append(TextUtils.format(text))
        return this
    }

    /**
     * Appends text that displays a tooltip when hovered over.
     *
     * @param text The main text to display.
     * @param hoverText The text to show inside the hover tooltip.
     */
    fun appendHoverText(text: String, hoverText: String): SmartMessage {
        val part = Component.empty().append(TextUtils.format(text))
        val hoverPart = TextUtils.format(hoverText)

        part.withStyle { style ->
            style.withHoverEvent(HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverPart))
        }

        rootComponent.append(part)
        return this
    }

    /**
     * Appends text that displays an item's actual tooltip when hovered over.
     * Perfect for announcing legendary drops or rewards.
     *
     * @param text The main text to display (e.g., "[Excalibur]").
     * @param itemStack The ItemStack whose stats will be previewed.
     */
    fun appendHoverItem(text: String, itemStack: ItemStack): SmartMessage {
        val part = Component.empty().append(TextUtils.format(text))
        val itemInfo = HoverEvent.ItemStackInfo(itemStack)

        part.withStyle { style ->
            style.withHoverEvent(HoverEvent(HoverEvent.Action.SHOW_ITEM, itemInfo))
        }

        rootComponent.append(part)
        return this
    }

    /**
     * Appends clickable text that executes a command when clicked by the player.
     * Optionally supports a hover tooltip.
     *
     * @param text The text to click.
     * @param command The command to execute (e.g., "/claim reward").
     * @param hoverText Optional text to show when hovering over the clickable text.
     */
    fun appendClickableCommand(text: String, command: String, hoverText: String? = null): SmartMessage {
        val part = Component.empty().append(TextUtils.format(text))

        part.withStyle { style ->
            var finalStyle = style.withClickEvent(ClickEvent(ClickEvent.Action.RUN_COMMAND, command))
            if (hoverText != null) {
                finalStyle = finalStyle.withHoverEvent(
                    HoverEvent(HoverEvent.Action.SHOW_TEXT, TextUtils.format(hoverText))
                )
            }
            finalStyle
        }

        rootComponent.append(part)
        return this
    }

    /**
     * Builds and returns the final Minecraft Component.
     *
     * @return The fully constructed MutableComponent.
     */
    fun build(): Component {
        return rootComponent
    }

    /**
     * Sends this interactive message to a specific player.
     *
     * @param player The target player.
     */
    fun send(player: ServerPlayer) {
        player.sendSystemMessage(rootComponent)
    }

    /**
     * Broadcasts this interactive message to all players on the server.
     *
     * @param server The Minecraft server instance.
     */
    fun broadcast(server: MinecraftServer) {
        server.playerList.broadcastSystemMessage(rootComponent, false)
    }
}