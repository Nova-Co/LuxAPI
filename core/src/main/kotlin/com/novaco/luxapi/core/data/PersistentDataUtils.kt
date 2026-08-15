package com.novaco.luxapi.core.data

import net.minecraft.core.component.DataComponents
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.component.CustomData

/**
 * Generic helper for attaching arbitrary NBT data to an ItemStack, independent of any
 * fixed schema. Consumers decide their own keys and value shapes.
 */
object PersistentDataUtils {

    /**
     * Returns a copy of the raw custom-data tag stored on [stack], or an empty tag if none.
     */
    fun read(stack: ItemStack): CompoundTag {
        return stack.get(DataComponents.CUSTOM_DATA)?.copyTag() ?: CompoundTag()
    }

    /**
     * Mutates the custom-data tag on [stack] in place via [mutator].
     */
    fun update(stack: ItemStack, mutator: (CompoundTag) -> Unit) {
        CustomData.update(DataComponents.CUSTOM_DATA, stack, mutator)
    }

    /**
     * Returns true if [stack]'s custom data contains [key].
     */
    fun has(stack: ItemStack, key: String): Boolean {
        return stack.get(DataComponents.CUSTOM_DATA)?.contains(key) ?: false
    }
}
