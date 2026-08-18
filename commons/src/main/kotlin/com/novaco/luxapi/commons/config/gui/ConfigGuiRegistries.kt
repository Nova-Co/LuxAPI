package com.novaco.luxapi.commons.config.gui

import com.novaco.luxapi.commons.config.gui.action.BackClickAction
import com.novaco.luxapi.commons.config.gui.action.CloseMenuClickAction
import com.novaco.luxapi.commons.config.gui.action.CooldownClickAction
import com.novaco.luxapi.commons.config.gui.action.ExecuteCommandsClickAction
import com.novaco.luxapi.commons.config.gui.action.GiveItemClickAction
import com.novaco.luxapi.commons.config.gui.action.OpenMenuClickAction
import com.novaco.luxapi.commons.config.gui.action.RefreshMenuClickAction
import com.novaco.luxapi.commons.config.gui.action.SendMessageClickAction
import com.novaco.luxapi.commons.config.gui.action.SoundClickAction
import com.novaco.luxapi.commons.config.gui.rule.CooldownDisplayRule
import com.novaco.luxapi.commons.config.gui.rule.PlaceholderConditionDisplayRule
import com.novaco.luxapi.commons.config.gui.rule.RequiresPermissionDisplayRule
import com.novaco.luxapi.commons.config.gui.rule.ToggleDisplayRule
import com.novaco.luxapi.commons.config.serializer.ConfigTypeSerializerRegistry
import com.novaco.luxapi.commons.config.serializer.PolymorphicConfigRegistry

/**
 * Wires up the [ClickAction] and [ItemDisplayRule] discriminated-union registries and their
 * built-in implementations. A platform bootstrap must call [init] once before loading any config
 * containing [ConfigMenuItem] — same requirement as registering any other custom type serializer,
 * just bundled here so it's one call instead of two.
 */
object ConfigGuiRegistries {

    val clickActions = PolymorphicConfigRegistry<ClickAction>()
    val displayRules = PolymorphicConfigRegistry<ItemDisplayRule>()

    /** Set by platform bootstrap so [ExecuteCommandsClickAction] can actually run commands. */
    var commandDispatcher: CommandDispatcher? = null

    /** Set by platform bootstrap so [GiveItemClickAction] can actually hand out items. */
    var itemGiver: ItemGiver? = null

    /** Set by platform bootstrap so [SoundClickAction] can actually play sounds. */
    var soundPlayer: SoundPlayer? = null

    private var initialized = false

    @Synchronized
    fun init() {
        if (initialized) return
        initialized = true

        clickActions.register("close", CloseMenuClickAction::class.java)
        clickActions.register("commands", ExecuteCommandsClickAction::class.java)
        clickActions.register("open_menu", OpenMenuClickAction::class.java)
        clickActions.register("back", BackClickAction::class.java)
        clickActions.register("refresh", RefreshMenuClickAction::class.java)
        clickActions.register("message", SendMessageClickAction::class.java)
        clickActions.register("give_item", GiveItemClickAction::class.java)
        clickActions.register("sound", SoundClickAction::class.java)
        clickActions.register("cooldown", CooldownClickAction::class.java)

        displayRules.register("requires_permission", RequiresPermissionDisplayRule::class.java)
        displayRules.register("placeholder_equals", PlaceholderConditionDisplayRule::class.java)
        displayRules.register("toggle", ToggleDisplayRule::class.java)
        displayRules.register("cooldown", CooldownDisplayRule::class.java)

        ConfigTypeSerializerRegistry.register(ClickAction::class.java, clickActions.serializer(ClickAction::class.java))
        ConfigTypeSerializerRegistry.register(ItemDisplayRule::class.java, displayRules.serializer(ItemDisplayRule::class.java))
    }
}
