package com.novaco.luxapi.fabric.gui

import com.novaco.luxapi.commons.gui.GuiItem
import com.novaco.luxapi.commons.gui.Gui
import com.novaco.luxapi.commons.player.LuxPlayer
import net.minecraft.core.component.DataComponents
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.SimpleContainer
import net.minecraft.world.SimpleMenuProvider
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.component.ItemLore

/**
 * Represents a functional Fabric-based graphical user interface.
 * Handles the mapping between generic GUI items and native Minecraft ItemStacks.
 */
open class FabricGui(
    val title: String,
    val rows: Int,
    initialItems: Map<Int, GuiItem>
) : Gui {

    val container = SimpleContainer(rows * 9)
    private val itemsMap = mutableMapOf<Int, GuiItem>()

    init {
        initialItems.forEach { (slot, item) ->
            setItem(slot, item)
        }
    }

    override fun open(player: LuxPlayer) {
        val serverPlayer = player.parent as? ServerPlayer ?: return
        val provider = SimpleMenuProvider(
            { id, inventory, _ -> LuxMenu(id, inventory, this) },
            Component.literal(title)
        )
        serverPlayer.openMenu(provider)
    }

    override fun close(player: LuxPlayer) {
        val serverPlayer = player.parent as? ServerPlayer ?: return
        serverPlayer.closeContainer()
    }

    override fun setItem(slot: Int, item: GuiItem) {
        itemsMap[slot] = item
        container.setItem(slot, buildItemStack(item))
    }

    override fun getItem(slot: Int): GuiItem? {
        return itemsMap[slot]
    }

    /**
     * Converts a generic GuiItem into a native Minecraft 1.21.1 ItemStack.
     * Utilizes modern Data Components for metadata injection.
     */
    protected fun buildItemStack(guiItem: GuiItem): ItemStack {
        val resourceLocation = ResourceLocation.tryParse(guiItem.material)
            ?: ResourceLocation.withDefaultNamespace("stone")

        val item = BuiltInRegistries.ITEM.get(resourceLocation)
        val itemStack = ItemStack(item)

        if (guiItem.displayName.isNotEmpty()) {
            itemStack.set(DataComponents.CUSTOM_NAME, Component.literal(guiItem.displayName))
        }

        if (guiItem.lore.isNotEmpty()) {
            val loreComponents = guiItem.lore.map { Component.literal(it) }
            itemStack.set(DataComponents.LORE, ItemLore(loreComponents))
        }

        return itemStack
    }

    /**
     * Refreshes all ItemStacks within the native container and synchronizes with the player.
     */
    override fun refresh(player: LuxPlayer) {
        val serverPlayer = player.parent as? ServerPlayer ?: return

        // Update native container slots based on the current LuxAPI itemsMap
        itemsMap.forEach { (slot, guiItem) ->
            container.setItem(slot, buildItemStack(guiItem))
        }

        // Minecraft 1.21.1: This triggers packet synchronization for the open menu
        serverPlayer.containerMenu.sendAllDataToRemote()
    }
}