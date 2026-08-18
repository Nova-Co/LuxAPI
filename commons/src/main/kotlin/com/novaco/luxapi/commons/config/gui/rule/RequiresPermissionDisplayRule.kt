package com.novaco.luxapi.commons.config.gui.rule

import com.novaco.luxapi.commons.config.gui.ConfigMenuItem
import com.novaco.luxapi.commons.config.gui.ItemDisplayRule
import com.novaco.luxapi.commons.player.LuxPlayer
import org.spongepowered.configurate.objectmapping.ConfigSerializable

/**
 * Hides (or swaps) an item unless the viewer has [permission]. Registered under id
 * `"requires_permission"`.
 */
@ConfigSerializable
class RequiresPermissionDisplayRule : ItemDisplayRule {

    var permission: String = ""
    var elseItem: ConfigMenuItem? = null

    override fun id(): String = "requires_permission"

    override fun resolve(player: LuxPlayer, current: ConfigMenuItem): ConfigMenuItem? {
        if (permission.isBlank() || player.hasPermission(permission)) return current
        return elseItem
    }
}
