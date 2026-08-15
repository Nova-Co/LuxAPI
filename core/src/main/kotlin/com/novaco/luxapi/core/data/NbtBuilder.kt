package com.novaco.luxapi.core.data

import net.minecraft.nbt.CompoundTag

/**
 * A small fluent builder over [CompoundTag], for hand-assembling NBT without the
 * verbose `putX` call chain. Complements [PersistentDataUtils] for item-stored data.
 */
class NbtBuilder {

    private val tag = CompoundTag()

    fun putString(key: String, value: String) = apply { tag.putString(key, value) }
    fun putInt(key: String, value: Int) = apply { tag.putInt(key, value) }
    fun putDouble(key: String, value: Double) = apply { tag.putDouble(key, value) }
    fun putBoolean(key: String, value: Boolean) = apply { tag.putBoolean(key, value) }
    fun putIntArray(key: String, value: IntArray) = apply { tag.putIntArray(key, value) }
    fun putTag(key: String, value: CompoundTag) = apply { tag.put(key, value) }

    fun build(): CompoundTag = tag
}
