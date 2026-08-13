package com.novaco.luxapi.cobblemon.drop

import com.cobblemon.mod.common.api.drop.DropEntry
import com.cobblemon.mod.common.api.drop.DropTable
import com.cobblemon.mod.common.api.events.drops.LootDroppedEvent
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.LivingEntity

/**
 * A simplified view of a Cobblemon [LootDroppedEvent], as delivered by
 * [DropInterceptor.onLootDropped]. Fired after a drop list has been chosen from a
 * [DropTable] but before the drops are actually performed.
 *
 * @property table The [DropTable] the drops were calculated from.
 * @property player The player the drop is targeting, if any.
 * @property entity The entity causing the drop (e.g. a fainted Pokémon), if any.
 * @property drops The final drop list. Mutate this list (add/remove) to change what
 * actually gets dropped.
 */
data class DropInterceptEvent(
    val table: DropTable,
    val player: ServerPlayer?,
    val entity: LivingEntity?,
    val drops: MutableList<DropEntry>,
    private val source: LootDroppedEvent
) {
    /** Prevents anything in [drops] from being dropped. */
    fun cancel() {
        source.cancel()
    }
}
