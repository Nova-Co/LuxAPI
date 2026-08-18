package com.novaco.luxapi.commons.config.gui

import com.novaco.luxapi.commons.gui.PaginatedGuiBuilder
import com.novaco.luxapi.commons.player.LuxPlayer
import org.spongepowered.configurate.objectmapping.ConfigSerializable

/**
 * [ConfigGuiInterface] extended with a paginated content pool and navigation buttons, converting
 * into a [PaginatedGuiBuilder] via [populatePaginated].
 */
@ConfigSerializable
class PaginatedConfigGuiInterface : ConfigGuiInterface() {

    var contentPositions: MutableList<Int> = mutableListOf()
    var globalItems: MutableList<ConfigMenuItem> = mutableListOf()
    var nextButtonSlot: Int = -1
    var nextButtonItem: ConfigMenuItem? = null
    var previousButtonSlot: Int = -1
    var previousButtonItem: ConfigMenuItem? = null

    fun populatePaginated(builder: PaginatedGuiBuilder, player: LuxPlayer): PaginatedGuiBuilder {
        populate(builder, player)
        builder.contentSlots(contentPositions)

        val globalGuiItems = globalItems.mapNotNull { it.toGuiItem(player) }
        if (globalGuiItems.isNotEmpty()) {
            builder.globalItems(globalGuiItems)
        }

        if (nextButtonSlot >= 0) {
            nextButtonItem?.toGuiItem(player)?.let { builder.nextButton(nextButtonSlot, it) }
        }
        if (previousButtonSlot >= 0) {
            previousButtonItem?.toGuiItem(player)?.let { builder.previousButton(previousButtonSlot, it) }
        }

        return builder
    }
}
