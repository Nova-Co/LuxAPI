package com.novaco.luxapi.commons.config.gui

import com.novaco.luxapi.commons.LuxAPI
import com.novaco.luxapi.commons.event.Subscribe
import com.novaco.luxapi.commons.event.player.PlayerQuitEvent
import com.novaco.luxapi.commons.gui.ClickType
import com.novaco.luxapi.commons.gui.Gui
import com.novaco.luxapi.commons.gui.GuiClickEvent
import com.novaco.luxapi.commons.player.LuxPlayer
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/**
 * Opens registered menus (see [MenuRegistry]) for a player while tracking navigation, so
 * [action.BackClickAction] can return to whatever menu the player came from and
 * [action.RefreshMenuClickAction] can re-show the current one. This is the entry point
 * [action.OpenMenuClickAction]/[action.BackClickAction]/[action.RefreshMenuClickAction] use — a
 * menu built and opened without going through here (e.g. a command handler's very first
 * `menu.populate(...).build().open(player)`) won't participate in back-navigation or fire
 * [ConfigGuiInterface.onOpen].
 */
object MenuNavigator {

    private val currentMenu = ConcurrentHashMap<UUID, String>()
    private val history = ConcurrentHashMap<UUID, ArrayDeque<String>>()

    /** Opens [menuId] for [player], pushing whatever menu they were just on onto their back-stack. */
    fun open(player: LuxPlayer, menuId: String) {
        val menu = MenuRegistry.get(menuId) ?: return

        currentMenu[player.uniqueId]?.let { previous ->
            if (previous != menuId) {
                history.computeIfAbsent(player.uniqueId) { ArrayDeque() }.addLast(previous)
            }
        }
        currentMenu[player.uniqueId] = menuId
        show(menu, player)
    }

    /** Returns to whichever menu is on top of [player]'s back-stack, if any. */
    fun back(player: LuxPlayer) {
        val previousId = history[player.uniqueId]?.removeLastOrNull() ?: return
        val menu = MenuRegistry.get(previousId) ?: return
        currentMenu[player.uniqueId] = previousId
        show(menu, player)
    }

    /** Re-shows [player]'s current menu without touching their back-stack. */
    fun refresh(player: LuxPlayer) {
        val menuId = currentMenu[player.uniqueId] ?: return
        val menu = MenuRegistry.get(menuId) ?: return
        show(menu, player)
    }

    /** Clears navigation state for [player] — call on logout to avoid an unbounded per-player leak. */
    fun clear(player: LuxPlayer) {
        currentMenu.remove(player.uniqueId)
        history.remove(player.uniqueId)
    }

    /**
     * Auto-cleanup on disconnect, same pattern as [com.novaco.luxapi.commons.metadata.PlayerMetadataManager].
     * Only fires once [LuxAPI.init] has registered this object with the event bus.
     */
    @Subscribe
    internal fun onPlayerQuit(event: PlayerQuitEvent) {
        clear(event.player)
    }

    private fun show(menu: ConfigGuiInterface, player: LuxPlayer) {
        val gui: Gui = if (menu is PaginatedConfigGuiInterface) {
            menu.populatePaginated(LuxAPI.createPaginatedMenu(), player).build()
        } else {
            menu.populate(LuxAPI.createMenu(), player).build()
        }

        gui.open(player)

        if (menu.onOpen.isNotEmpty()) {
            val event = GuiClickEvent(player, -1, ClickType.UNKNOWN, gui)
            for (action in menu.onOpen) {
                if (!action.handle(event)) break
            }
        }
    }
}
