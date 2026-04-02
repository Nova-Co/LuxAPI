package com.novaco.luxapi.commons

import com.novaco.luxapi.commons.chat.placeholder.DefaultPlayerProvider
import com.novaco.luxapi.commons.chat.placeholder.PlaceholderManager
import com.novaco.luxapi.commons.gui.GuiBuilder
import com.novaco.luxapi.commons.gui.PaginatedGuiBuilder
import com.novaco.luxapi.commons.scheduler.LuxScheduler

/**
 * The primary entry point for developers utilizing the LuxAPI framework.
 * Provides global access to cross-platform services and builders.
 */
object LuxAPI {

    fun initDefaultPlaceholders() {
        PlaceholderManager.register(DefaultPlayerProvider())
    }

    var guiProvider: () -> GuiBuilder = {
        throw IllegalStateException("LuxAPI GUI Provider has not been initialized by the platform!")
    }

    var paginatedGuiProvider: () -> PaginatedGuiBuilder = {
        throw IllegalStateException("LuxAPI Paginated GUI Provider has not been initialized by the platform!")
    }

    /**
     * Creates a new instance of a cross-platform graphical user interface builder.
     *
     * @return A platform-specific implementation of LuxGuiBuilder.
     */
    fun createMenu(): GuiBuilder {
        return guiProvider()
    }

    /**
     * Creates a new instance of a cross-platform paginated graphical user interface builder.
     *
     * @return A platform-specific implementation of PaginatedGuiBuilder.
     */
    fun createPaginatedMenu(): PaginatedGuiBuilder {
        return paginatedGuiProvider()
    }

    var schedulerProvider: () -> LuxScheduler = {
        throw IllegalStateException("LuxAPI Scheduler Provider has not been initialized!")
    }

    /**
     * Gets the platform-specific scheduler instance.
     */
    fun getScheduler(): LuxScheduler {
        return schedulerProvider()
    }
}