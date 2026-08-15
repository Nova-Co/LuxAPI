package com.novaco.luxapi.core.item

import com.mojang.authlib.properties.Property
import com.mojang.authlib.properties.PropertyMap
import com.novaco.luxapi.core.text.TextUtils
import net.minecraft.core.Holder
import net.minecraft.core.component.DataComponents
import net.minecraft.network.chat.Component
import net.minecraft.util.Unit as McUnit
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.component.CustomModelData
import net.minecraft.world.item.component.ItemLore
import net.minecraft.world.item.component.ResolvableProfile
import net.minecraft.world.item.component.Unbreakable
import net.minecraft.world.item.enchantment.Enchantment
import java.util.Optional
import java.util.UUID

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
     * Adds an enchantment at the given level.
     */
    fun enchant(enchantment: Holder<Enchantment>, level: Int): ItemBuilder {
        itemStack.enchant(enchantment, level)
        return this
    }

    /**
     * Hides the default "+X" attribute/enchantment tooltip lines added by the game.
     */
    fun hideAdditionalTooltip(hide: Boolean = true): ItemBuilder {
        if (hide) {
            itemStack.set(DataComponents.HIDE_ADDITIONAL_TOOLTIP, McUnit.INSTANCE)
        } else {
            itemStack.remove(DataComponents.HIDE_ADDITIONAL_TOOLTIP)
        }
        return this
    }

    /**
     * Forces the enchantment glint on/off regardless of actual enchantments.
     * Pass null to clear the override and use default behavior.
     */
    fun glint(forceGlint: Boolean?): ItemBuilder {
        if (forceGlint == null) {
            itemStack.remove(DataComponents.ENCHANTMENT_GLINT_OVERRIDE)
        } else {
            itemStack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, forceGlint)
        }
        return this
    }

    /**
     * Turns this item into a player head displaying [name]/[uuid]'s skin.
     * Only meaningful when the underlying item is a player head.
     */
    fun skullOwner(name: String? = null, uuid: UUID? = null): ItemBuilder {
        itemStack.set(
            DataComponents.PROFILE,
            ResolvableProfile(Optional.ofNullable(name), Optional.ofNullable(uuid), PropertyMap())
        )
        return this
    }

    /**
     * Turns this item into a player head with a custom base64 skin texture value.
     * Only meaningful when the underlying item is a player head.
     */
    fun skullTexture(base64Texture: String): ItemBuilder {
        val properties = PropertyMap()
        properties.put("textures", Property("textures", base64Texture))
        itemStack.set(
            DataComponents.PROFILE,
            ResolvableProfile(Optional.empty(), Optional.empty(), properties)
        )
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