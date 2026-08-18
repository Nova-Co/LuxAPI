package com.novaco.luxapi.commons.config.gui

import com.novaco.luxapi.commons.config.serializer.PolymorphicConfigEntry
import com.novaco.luxapi.commons.player.LuxPlayer
import org.spongepowered.configurate.objectmapping.ConfigSerializable

/**
 * A declaratively-configured rule that can alter or hide a [ConfigMenuItem] per-viewer.
 * Implementations are resolved by discriminator id via [ConfigGuiRegistries] — register custom
 * ones with `ConfigGuiRegistries.displayRules.register(id, YourRule::class.java)` before loading
 * any config that references them.
 */
@ConfigSerializable
interface ItemDisplayRule : PolymorphicConfigEntry {

    /**
     * Returns the item [current] should resolve to for [player] — [current] itself to leave it
     * unchanged, a replacement, or null to hide it entirely.
     */
    fun resolve(player: LuxPlayer, current: ConfigMenuItem): ConfigMenuItem?
}
