package com.novaco.luxapi.bukkit.gui

import com.novaco.luxapi.bukkit.player.BukkitLuxPlayer
import com.novaco.luxapi.commons.gui.Gui
import com.novaco.luxapi.commons.gui.GuiItem
import com.novaco.luxapi.commons.player.LuxPlayer
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.Inventory

/**
 * Bukkit-native implementation of [Gui], backed by a real [org.bukkit.inventory.Inventory]
 * (chest UI) — no custom container/menu class needed, unlike the Fabric/NeoForge equivalents
 * which have to build one against raw NMS.
 */
open class BukkitGui(
    val title: String,
    val rows: Int,
    initialItems: Map<Int, GuiItem>
) : Gui {

    val holder = LuxGuiHolder(this)
    val inventory: Inventory = Bukkit.createInventory(holder, rows * 9, title).also { holder.bind(it) }
    private val itemsMap = mutableMapOf<Int, GuiItem>()

    init {
        initialItems.forEach { (slot, item) -> setItem(slot, item) }
    }

    override fun open(player: LuxPlayer) {
        val bukkitPlayer = player.parent as? Player ?: return
        val currentHolder = bukkitPlayer.openInventory.topInventory.holder as? LuxGuiHolder

        if (currentHolder != null && currentHolder.inventory.size == inventory.size) {
            val targetGui = currentHolder.gui
            itemsMap.forEach { (slot, item) -> targetGui.setItem(slot, item) }
            targetGui.refresh(player)
            return
        }

        bukkitPlayer.openInventory(inventory)
    }

    override fun close(player: LuxPlayer) {
        val bukkitPlayer = player.parent as? Player ?: return
        bukkitPlayer.closeInventory()
    }

    override fun setItem(slot: Int, item: GuiItem) {
        itemsMap[slot] = item
        inventory.setItem(slot, buildItemStack(item))
    }

    override fun getItem(slot: Int): GuiItem? = itemsMap[slot]

    /**
     * Converts a generic [GuiItem] into a native Bukkit [ItemStack].
     */
    protected fun buildItemStack(guiItem: GuiItem): ItemStack {
        val itemStack = (guiItem.stack as? ItemStack)?.clone() ?: run {
            val material = Material.matchMaterial(guiItem.material ?: "") ?: Material.STONE
            ItemStack(material)
        }

        val meta = itemStack.itemMeta
        if (meta != null) {
            if (guiItem.displayName.isNotEmpty()) {
                meta.setDisplayName(guiItem.displayName)
            }
            if (guiItem.lore.isNotEmpty()) {
                meta.lore = guiItem.lore
            }
            if (guiItem.customModelData != 0) {
                meta.setCustomModelData(guiItem.customModelData)
            }
            itemStack.itemMeta = meta
        }

        return itemStack
    }

    override fun refresh(player: LuxPlayer) {
        itemsMap.forEach { (slot, guiItem) -> inventory.setItem(slot, buildItemStack(guiItem)) }
        (player.parent as? Player)?.updateInventory()
    }

    /**
     * Refreshes every currently-tracked viewer, not just one. Unlike Fabric/NeoForge, Bukkit's
     * [Inventory] already tracks its own viewers ([Inventory.getViewers]) — one shared
     * [inventory] backs every player who has this GUI open, so no separate tracking is needed.
     */
    override fun refreshAll() {
        itemsMap.forEach { (slot, guiItem) -> inventory.setItem(slot, buildItemStack(guiItem)) }
        inventory.viewers.forEach { (it as? Player)?.updateInventory() }
    }

    override fun hasViewers(): Boolean = inventory.viewers.isNotEmpty()
}
