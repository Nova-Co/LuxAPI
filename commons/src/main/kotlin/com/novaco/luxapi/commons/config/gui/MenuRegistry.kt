package com.novaco.luxapi.commons.config.gui

import com.novaco.luxapi.commons.registry.Registry

/**
 * Names menus so [action.OpenMenuClickAction] (and [MenuNavigator] generally) can reference one
 * by id instead of holding an object reference. A platform bootstrap registers each loaded menu
 * once, e.g. `MenuRegistry.register("hub", hubMenuConfig)`.
 */
object MenuRegistry {

    private val menus = Registry<String, ConfigGuiInterface>()

    fun register(id: String, menu: ConfigGuiInterface) {
        menus.register(id, menu)
    }

    fun get(id: String): ConfigGuiInterface? = menus.get(id)
}
