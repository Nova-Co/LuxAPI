package com.novaco.luxapi.commons

import com.novaco.luxapi.commons.chat.placeholder.DefaultPlayerProvider
import com.novaco.luxapi.commons.chat.placeholder.PlaceholderManager
import com.novaco.luxapi.commons.event.EventBus
import com.novaco.luxapi.commons.gui.GuiBuilder
import com.novaco.luxapi.commons.gui.PaginatedGuiBuilder
import com.novaco.luxapi.commons.metadata.PlayerMetadataManager
import com.novaco.luxapi.commons.scheduler.LuxScheduler
import com.novaco.luxapi.commons.service.ServiceManager

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

    /**
     * Access point for the ServiceManager.
     */
    val serviceManager = ServiceManager

    /**
     * Registers a service using Kotlin's reified type parameters for a cleaner syntax.
     */
    inline fun <reified T : Any> registerService(provider: T) {
        serviceManager.register(T::class.java, provider)
    }

    /**
     * Retrieves a service using Kotlin's reified type parameters.
     * Returns null if the service is not currently registered.
     */
    inline fun <reified T : Any> getService(): T? {
        return serviceManager.get(T::class.java)
    }

    /**
     * Checks if a specific service is available.
     */
    inline fun <reified T : Any> hasService(): Boolean {
        return serviceManager.has(T::class.java)
    }

    /**
     * Initializes core LuxAPI services.
     * Must be called during server startup by the platform implementation.
     */
    fun init() {
        EventBus.register(PlayerMetadataManager)
        PlaceholderManager.register(DefaultPlayerProvider())
    }
}