package com.novaco.luxapi.cobblemon.item

import com.cobblemon.mod.common.item.PokemonItem
import com.cobblemon.mod.common.pokemon.Pokemon
import net.minecraft.core.component.DataComponents
import net.minecraft.network.chat.Component
import net.minecraft.world.item.ItemStack

/**
 * A builder class inspired by EnvyAPI's SpriteBuilder, adapted for Minecraft 1.21.1 Data Components.
 * Transforms a Cobblemon [Pokemon] into a visual UI [ItemStack] with custom lore and display names.
 *
 */
class PokemonItemBuilder(private val pokemon: Pokemon) {

    private var displayName: Component? = null
    private val lore: MutableList<Component> = mutableListOf()
    private var amount: Int = 1

    /**
     * Sets the custom display name for the item.
     * (e.g., Component.literal("§6★ Shiny Charizard ★"))
     */
    fun setName(name: Component): PokemonItemBuilder {
        this.displayName = name
        return this
    }

    /**
     * Adds a single line of lore to the item.
     */
    fun addLore(line: Component): PokemonItemBuilder {
        this.lore.add(line)
        return this
    }

    /**
     * Adds multiple lines of lore to the item at once.
     */
    fun addLore(lines: List<Component>): PokemonItemBuilder {
        this.lore.addAll(lines)
        return this
    }

    /**
     * Sets the stack size of the item.
     */
    fun setAmount(amount: Int): PokemonItemBuilder {
        this.amount = amount
        return this
    }

    /**
     * Compiles the configurations and builds the final ItemStack.
     * Uses Cobblemon's native PokemonItem factory and Minecraft 1.21.1 Data Components.
     */
    fun build(): ItemStack {
        val stack = PokemonItem.from(pokemon, amount)

        displayName?.let {
            stack.set(DataComponents.CUSTOM_NAME, it)
        }

        if (lore.isNotEmpty()) {
            val loreComponent = net.minecraft.world.item.component.ItemLore(lore)
            stack.set(DataComponents.LORE, loreComponent)
        }

        return stack
    }
}

/**
 * Extension function for quick creation without instantiating the builder manually.
 * Usage: val item = myPokemon.toUIItem { addLore(...) }
 */
inline fun Pokemon.toUIItem(block: PokemonItemBuilder.() -> Unit = {}): ItemStack {
    val builder = PokemonItemBuilder(this)
    builder.block()
    return builder.build()
}