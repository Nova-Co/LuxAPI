package com.novaco.luxapi.cobblemon.drop

import com.cobblemon.mod.common.api.events.CobblemonEvents
import com.cobblemon.mod.common.api.reactive.ObservableSubscription
import com.cobblemon.mod.common.api.events.drops.LootDroppedEvent

/**
 * A hook into every [com.cobblemon.mod.common.api.drop.DropTable] drop Cobblemon performs
 * (Pokémon faint drops, catch drops, and any other code path that calls
 * `DropTable.drop`/`DropTable.postLootDroppedEvent`), fired after the drop list has been
 * chosen but before the drops are actually placed in the world.
 */
object DropInterceptor {

    /**
     * Registers a listener that runs for every drop-table drop attempt. Call
     * [DropInterceptEvent.cancel] from within [listener] to prevent that drop entirely,
     * or mutate [DropInterceptEvent.drops] to change what gets dropped.
     *
     * @param listener Runs once per drop attempt.
     * @return The subscription handle. Cobblemon's event bus has no automatic listener
     * lifecycle — call `.unsubscribe()` on it if [listener] shouldn't outlive its caller
     * (e.g. a reloadable module registering this on every reload).
     */
    fun onLootDropped(listener: (DropInterceptEvent) -> Unit): ObservableSubscription<LootDroppedEvent> {
        return CobblemonEvents.LOOT_DROPPED.subscribe { event ->
            listener(
                DropInterceptEvent(
                    table = event.table,
                    player = event.player,
                    entity = event.entity,
                    drops = event.drops,
                    source = event
                )
            )
        }
    }
}
