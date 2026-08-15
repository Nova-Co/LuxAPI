package com.novaco.luxapi.bukkit.item

import com.novaco.luxapi.commons.extensions.colorize
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta
import org.bukkit.persistence.PersistentDataType
import java.util.UUID

/**
 * A fluent builder for creating and modifying Bukkit ItemStacks.
 * The Bukkit-side counterpart of [com.novaco.luxapi.core.item.ItemBuilder]'s API shape,
 * adapted to [org.bukkit.inventory.meta.ItemMeta] since Bukkit has no NMS Data Components access.
 */
class BukkitItemBuilder(material: Material, amount: Int = 1) {

    private val itemStack = ItemStack(material, amount)
    private val meta = itemStack.itemMeta
    private val loreLines = mutableListOf<String>()

    /**
     * Sets the amount of the item.
     */
    fun amount(amount: Int): BukkitItemBuilder {
        itemStack.amount = amount
        return this
    }

    /**
     * Sets the custom display name of the item (supports `&` color codes and hex via [colorize]).
     */
    fun name(text: String): BukkitItemBuilder {
        meta?.setDisplayName(text.colorize())
        return this
    }

    /**
     * Adds a single line of lore to the item (supports `&` color codes and hex via [colorize]).
     */
    fun addLore(line: String): BukkitItemBuilder {
        loreLines.add(line.colorize())
        return this
    }

    /**
     * Adds multiple lines of lore at once.
     */
    fun lore(vararg lines: String): BukkitItemBuilder {
        lines.forEach { addLore(it) }
        return this
    }

    /**
     * Sets the CustomModelData integer for resource packs.
     */
    fun customModelData(value: Int): BukkitItemBuilder {
        meta?.setCustomModelData(value)
        return this
    }

    /**
     * Makes the item unbreakable.
     */
    fun unbreakable(isUnbreakable: Boolean = true): BukkitItemBuilder {
        meta?.isUnbreakable = isUnbreakable
        return this
    }

    /**
     * Adds an enchantment at the given level, bypassing normal level/target restrictions.
     */
    fun enchant(enchantment: Enchantment, level: Int): BukkitItemBuilder {
        meta?.addEnchant(enchantment, level, true)
        return this
    }

    /**
     * Hides the given tooltip sections (enchants, attributes, unbreakable, etc.).
     */
    fun flags(vararg flags: ItemFlag): BukkitItemBuilder {
        meta?.addItemFlags(*flags)
        return this
    }

    /**
     * Turns this item into a player head displaying [name]/[uuid]'s skin.
     * Only meaningful when the underlying material is [Material.PLAYER_HEAD].
     */
    fun skullOwner(name: String? = null, uuid: UUID? = null): BukkitItemBuilder {
        val skullMeta = meta as? SkullMeta ?: return this
        val offlinePlayer = when {
            uuid != null -> Bukkit.getOfflinePlayer(uuid)
            name != null -> Bukkit.getOfflinePlayer(name)
            else -> null
        }
        offlinePlayer?.let { skullMeta.owningPlayer = it }
        return this
    }

    /**
     * Turns this item into a player head with a custom base64 skin texture value.
     * Only meaningful when the underlying material is [Material.PLAYER_HEAD].
     *
     * Relies on reflection into CraftBukkit's skull meta implementation since plain
     * `spigot-api` exposes no public API for an arbitrary base64 texture (unlike Paper's
     * `PlayerProfile`). Fails silently (item is returned unmodified) if the server's
     * internal skull meta shape doesn't match what this reflects into.
     */
    fun skullTexture(base64Texture: String): BukkitItemBuilder {
        val skullMeta = meta as? SkullMeta ?: return this
        try {
            val profileClass = Class.forName("com.mojang.authlib.GameProfile")
            val profile = profileClass.getConstructor(UUID::class.java, String::class.java)
                .newInstance(UUID.randomUUID(), null)

            val propertyClass = Class.forName("com.mojang.authlib.properties.Property")
            val property = propertyClass.getConstructor(String::class.java, String::class.java)
                .newInstance("textures", base64Texture)

            val propertiesField = profileClass.getMethod("getProperties").invoke(profile)
            propertiesField.javaClass.getMethod("put", Any::class.java, Any::class.java)
                .invoke(propertiesField, "textures", property)

            val profileField = skullMeta.javaClass.getDeclaredField("profile")
            profileField.isAccessible = true
            profileField.set(skullMeta, profile)
        } catch (_: ReflectiveOperationException) {
            // Server's internal skull meta shape doesn't match — item returned unmodified.
        }
        return this
    }

    /**
     * Sets a typed value in the item's [org.bukkit.persistence.PersistentDataContainer].
     */
    fun <T, Z : Any> pdc(key: NamespacedKey, type: PersistentDataType<T, Z>, value: Z): BukkitItemBuilder {
        meta?.persistentDataContainer?.set(key, type, value)
        return this
    }

    /**
     * Finalizes and returns the native ItemStack.
     */
    fun build(): ItemStack {
        if (loreLines.isNotEmpty()) {
            meta?.lore = loreLines
        }
        itemStack.itemMeta = meta
        return itemStack
    }
}
