package com.novaco.luxapi.neoforge.gui

import com.novaco.luxapi.commons.gui.Gui
import com.novaco.luxapi.commons.gui.GuiItem
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
import java.util.concurrent.ConcurrentHashMap

/**
 * Represents a functional NeoForge-based graphical user interface.
 */
open class NeoForgeGui(
    val title: String,
    val rows: Int,
    initialItems: Map<Int, GuiItem>
) : Gui {

    val container = SimpleContainer(rows * 9)
    protected val itemsMap = mutableMapOf<Int, GuiItem>()
    override var generation: Int = 0
        protected set

    /**
     * Players who currently have this GUI open. Vanilla gives no reverse lookup from a
     * container back to its viewers, and multiple players can have this same [NeoForgeGui]
     * open simultaneously via separate [NeoForgeMenu] instances sharing [container] — so this
     * is tracked by hand. Kept in sync by [open]/[close] and by [NeoForgeMenu.removed] calling
     * [onViewerRemoved] for client-initiated closes (ESC, inventory swap, disconnect).
     */
    private val viewers: MutableSet<ServerPlayer> = ConcurrentHashMap.newKeySet()

    init {
        initialItems.forEach { (slot, item) ->
            setItem(slot, item)
        }
    }

    override fun open(player: LuxPlayer) {
        val serverPlayer = player.parent as? ServerPlayer ?: return
        val currentMenu = serverPlayer.containerMenu

        if (currentMenu is NeoForgeMenu && currentMenu.gui.rows == rows) {
            val targetGui = currentMenu.gui
            if (targetGui !== this) {
                targetGui.generation++
            }
            itemsMap.forEach { (slot, item) -> targetGui.setItem(slot, item) }
            targetGui.refresh(player)
            return
        }

        viewers.add(serverPlayer)
        val provider = SimpleMenuProvider(
            { id, inventory, _ -> NeoForgeMenu(id, inventory, this) },
            Component.literal(title)
        )
        serverPlayer.openMenu(provider)
    }

    override fun close(player: LuxPlayer) {
        val serverPlayer = player.parent as? ServerPlayer ?: return
        viewers.remove(serverPlayer)
        serverPlayer.closeContainer()
    }

    /**
     * Called by [NeoForgeMenu.removed] when a viewer's client closes this GUI without going
     * through [close] (ESC, inventory swap, disconnect), so [viewers] doesn't go stale.
     */
    internal fun onViewerRemoved(serverPlayer: ServerPlayer) {
        viewers.remove(serverPlayer)
    }

    override fun setItem(slot: Int, item: GuiItem) {
        itemsMap[slot] = item
        container.setItem(slot, buildItemStack(item))
    }

    override fun getItem(slot: Int): GuiItem? {
        return itemsMap[slot]
    }

    override fun refresh(player: LuxPlayer) {
        val serverPlayer = player.parent as? ServerPlayer ?: return
        syncContainer()
        serverPlayer.containerMenu.sendAllDataToRemote()
    }

    /**
     * Refreshes every currently-tracked viewer, not just one.
     */
    override fun refreshAll() {
        syncContainer()
        viewers.forEach { it.containerMenu.sendAllDataToRemote() }
    }

    override fun hasViewers(): Boolean = viewers.isNotEmpty()

    /**
     * Writes the current LuxAPI itemsMap into the native container. Shared by [refresh] and
     * [refreshAll] so a multi-viewer refresh doesn't redo this once per viewer.
     */
    private fun syncContainer() {
        itemsMap.forEach { (slot, guiItem) ->
            container.setItem(slot, buildItemStack(guiItem))
        }
    }

    protected fun buildItemStack(guiItem: GuiItem): ItemStack {
        val itemStack = (guiItem.stack as? ItemStack)?.copy() ?: run {
            val resourceLocation = ResourceLocation.tryParse(guiItem.material ?: "")
                ?: ResourceLocation.withDefaultNamespace("stone")
            ItemStack(BuiltInRegistries.ITEM.get(resourceLocation))
        }

        if (guiItem.displayName.isNotEmpty()) {
            itemStack.set(DataComponents.CUSTOM_NAME, Component.literal(guiItem.displayName))
        }

        if (guiItem.lore.isNotEmpty()) {
            val loreComponents = guiItem.lore.map { Component.literal(it) }
            itemStack.set(DataComponents.LORE, ItemLore(loreComponents))
        }

        return itemStack
    }
}