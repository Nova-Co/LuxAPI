package com.novaco.luxapi.core.item

import com.novaco.luxapi.core.text.TextUtils
import net.minecraft.core.component.DataComponents
import net.minecraft.network.chat.Component
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.component.CustomModelData
import net.minecraft.world.item.component.ItemLore
import net.minecraft.world.item.component.Unbreakable

/**
 * A fluent builder for creating and modifying Minecraft ItemStacks.
 * Fully compatible with Minecraft 1.21+ Data Components.
 */
class ItemBuilder(item: Item, count: Int = 1) {

    private val itemStack = ItemStack(item, count)
    private val loreLines = mutableListOf<Component>()

    /**
     * Sets the amount of the item.
     */
    fun amount(amount: Int): ItemBuilder {
        itemStack.count = amount
        return this
    }

    /**
     * Sets the custom display name of the item (Supports & color codes).
     */
    fun name(text: String): ItemBuilder {
        itemStack.set(DataComponents.CUSTOM_NAME, TextUtils.format(text))
        return this
    }

    /**
     * Adds a single line of lore to the item (Supports & color codes).
     */
    fun addLore(line: String): ItemBuilder {
        loreLines.add(TextUtils.format(line))
        return this
    }

    /**
     * Adds multiple lines of lore at once.
     */
    fun lore(vararg lines: String): ItemBuilder {
        lines.forEach { addLore(it) }
        return this
    }

    /**
     * Sets the CustomModelData integer for resource packs.
     */
    fun customModelData(value: Int): ItemBuilder {
        itemStack.set(DataComponents.CUSTOM_MODEL_DATA, CustomModelData(value))
        return this
    }

    /**
     * Makes the item unbreakable.
     */
    fun unbreakable(isUnbreakable: Boolean = true): ItemBuilder {
        if (isUnbreakable) {
            itemStack.set(DataComponents.UNBREAKABLE, Unbreakable(true))
        } else {
            itemStack.remove(DataComponents.UNBREAKABLE)
        }
        return this
    }

    /**
     * Finalizes and returns the native ItemStack.
     */
    fun build(): ItemStack {
        if (loreLines.isNotEmpty()) {
            itemStack.set(DataComponents.LORE, ItemLore(loreLines))
        }
        return itemStack
    }
}