package com.novaco.luxapi.cobblemon.drop

import com.cobblemon.mod.common.api.drop.DropTable
import com.cobblemon.mod.common.api.drop.ItemDropEntry
import net.minecraft.resources.ResourceLocation

/**
 * Convenience mutation helpers for a Cobblemon [DropTable] (e.g. `Species.drops`, which
 * Cobblemon already exposes as a public `var` — this wrapper doesn't add a getter/setter
 * for that, it adds validated entry-building on top of a table you already have).
 *
 * **Scope note:** [DropTable.amount] (the default drop-count range) is a `val` in
 * Cobblemon, not settable — only the [DropTable.entries] list itself is mutable, so
 * that's all this wrapper touches.
 */
object DropManager {

    /**
     * Adds an item drop entry to [table]. Returns false without touching the table if
     * [itemId] isn't a valid resource location (e.g. "cobblemon:poke_ball").
     */
    fun addItemDrop(
        table: DropTable,
        itemId: String,
        percentage: Float = 100F,
        quantity: Int = 1,
        maxSelectableTimes: Int = 1
    ): Boolean {
        val id = ResourceLocation.tryParse(itemId) ?: return false
        table.entries.add(ItemDropEntry().apply {
            this.item = id
            this.percentage = percentage
            this.quantity = quantity
            this.maxSelectableTimes = maxSelectableTimes
        })
        return true
    }

    /** Removes every entry from [table], leaving it with no drops. */
    fun clearDrops(table: DropTable) {
        table.entries.clear()
    }
}
