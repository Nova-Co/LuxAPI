package com.novaco.luxapi.bukkit.gui

import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder

/**
 * Marker [InventoryHolder] identifying a native Bukkit inventory as belonging to
 * a LuxAPI-managed [BukkitGui], so [BukkitGuiListener] can distinguish it from
 * arbitrary plugin/vanilla inventories sharing the same click event stream.
 */
class LuxGuiHolder(val gui: BukkitGui) : InventoryHolder {

    private lateinit var inventory: Inventory

    fun bind(inventory: Inventory) {
        this.inventory = inventory
    }

    override fun getInventory(): Inventory = inventory
}
