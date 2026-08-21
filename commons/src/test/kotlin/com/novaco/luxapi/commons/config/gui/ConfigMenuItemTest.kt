package com.novaco.luxapi.commons.config.gui

import com.novaco.luxapi.commons.config.gui.rule.RequiresPermissionDisplayRule
import com.novaco.luxapi.commons.gui.ClickType
import com.novaco.luxapi.commons.gui.Gui
import com.novaco.luxapi.commons.gui.GuiClickEvent
import com.novaco.luxapi.commons.gui.GuiItem
import com.novaco.luxapi.commons.math.Vector3D
import com.novaco.luxapi.commons.player.LuxPlayer
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.util.UUID

private class FakeLuxPlayer(private val permissions: Set<String> = emptySet()) : LuxPlayer {
    override val uniqueId: UUID = UUID.randomUUID()
    override val parent: Any = Any()
    override val locale: String = "en_us"
    override val position: Vector3D = Vector3D(0.0, 0.0, 0.0)
    override val name: String = "Fake"

    override fun sendTitle(title: String, subtitle: String) {}
    override fun kick(reason: String) {}
    override fun sendMessage(message: String) {}
    override fun hasPermission(permission: String): Boolean = permission in permissions
}

private class FakeGui : Gui {
    var closedFor: LuxPlayer? = null
    override fun open(player: LuxPlayer) {}
    override fun close(player: LuxPlayer) { closedFor = player }
    override fun setItem(slot: Int, item: GuiItem) {}
    override fun getItem(slot: Int): GuiItem? = null
    override fun refresh(player: LuxPlayer) {}
    override fun refreshAll() {}
    override fun hasViewers(): Boolean = true
    override val generation: Int = 0
}

class ConfigMenuItemTest {

    @Test
    fun `disabled item resolves to null`() {
        val item = ConfigMenuItem().apply { enabled = false }
        val player = FakeLuxPlayer()

        assertNull(item.resolve(player))
        assertNull(item.toGuiItem(player))
    }

    @Test
    fun `RequiresPermissionDisplayRule hides the item for a player lacking permission`() {
        val elseItem = ConfigMenuItem().apply { type = "minecraft:barrier" }
        val item = ConfigMenuItem().apply {
            type = "minecraft:diamond_sword"
            displayRules.add(RequiresPermissionDisplayRule().apply {
                permission = "lux.admin"
                this.elseItem = elseItem
            })
        }

        val withoutPermission = FakeLuxPlayer()
        val withPermission = FakeLuxPlayer(setOf("lux.admin"))

        assertEquals("minecraft:barrier", item.resolve(withoutPermission)?.type)
        assertEquals("minecraft:diamond_sword", item.resolve(withPermission)?.type)
    }

    @Test
    fun `close click action closes the gui for the clicking player`() {
        val gui = FakeGui()
        val player = FakeLuxPlayer()
        val item = ConfigMenuItem().apply {
            clickActions.add(com.novaco.luxapi.commons.config.gui.action.CloseMenuClickAction())
        }

        val guiItem = item.toGuiItem(player)!!
        guiItem.clickHandler?.invoke(GuiClickEvent(player, 0, ClickType.LEFT, gui))

        assertEquals(player, gui.closedFor)
    }
}
