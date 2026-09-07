package com.novaco.luxapi.cobblemon.spawning

import com.cobblemon.mod.common.api.events.CobblemonEvents
import com.cobblemon.mod.common.api.events.habitats.HabitatSpawnActivatedEvent
import com.cobblemon.mod.common.api.events.habitats.SaveHabitatBlockSettingsEvent
import com.cobblemon.mod.common.api.reactive.ObservableSubscription

/**
 * A hook into the habitat block's spawn-activation and settings-save lifecycle.
 */
object HabitatBlockInterceptor {

    /**
     * Registers a listener that runs when a habitat block is about to attempt a spawn.
     * Call [HabitatSpawnActivatedEvent.cancel] from within [listener] to prevent the
     * habitat block from spawning anything for this activation.
     *
     * @param listener Runs once per spawn activation.
     */
    fun onSpawnActivated(listener: (HabitatSpawnActivatedEvent) -> Unit): ObservableSubscription<HabitatSpawnActivatedEvent> {
        return CobblemonEvents.HABITAT_SPAWN_ACTIVATED.subscribe { event -> listener(event) }
    }

    /**
     * Registers a listener that runs before a player's habitat block settings are saved.
     * Call [SaveHabitatBlockSettingsEvent.Pre.cancel] from within [listener] to block the
     * save.
     *
     * @param listener Runs once per save attempt.
     */
    fun onSettingsSaving(listener: (SaveHabitatBlockSettingsEvent.Pre) -> Unit): ObservableSubscription<SaveHabitatBlockSettingsEvent.Pre> {
        return CobblemonEvents.HABITAT_SETTINGS_SAVED_PRE.subscribe { event -> listener(event) }
    }

    /**
     * Registers a listener that runs after a player's habitat block settings have been
     * saved. Not cancelable — [SaveHabitatBlockSettingsEvent.Post] carries no cancel
     * state, so this is a notification hook only.
     *
     * @param listener Runs once per completed save.
     */
    fun onSettingsSaved(listener: (SaveHabitatBlockSettingsEvent.Post) -> Unit): ObservableSubscription<SaveHabitatBlockSettingsEvent.Post> {
        return CobblemonEvents.HABITAT_SETTINGS_SAVED_POST.subscribe { event -> listener(event) }
    }
}
