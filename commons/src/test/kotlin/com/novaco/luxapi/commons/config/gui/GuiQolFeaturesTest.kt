package com.novaco.luxapi.commons.config.gui

import com.novaco.luxapi.commons.LuxAPI
import com.novaco.luxapi.commons.chat.placeholder.PlaceholderManager
import com.novaco.luxapi.commons.chat.placeholder.PlaceholderProvider
import com.novaco.luxapi.commons.config.gui.action.BackClickAction
import com.novaco.luxapi.commons.config.gui.action.CooldownClickAction
import com.novaco.luxapi.commons.config.gui.action.GiveItemClickAction
import com.novaco.luxapi.commons.config.gui.action.OpenMenuClickAction
import com.novaco.luxapi.commons.config.gui.action.RefreshMenuClickAction
import com.novaco.luxapi.commons.config.gui.action.SendMessageClickAction
import com.novaco.luxapi.commons.config.gui.action.SoundClickAction
import com.novaco.luxapi.commons.config.gui.rule.CooldownDisplayRule
import com.novaco.luxapi.commons.config.gui.rule.PlaceholderConditionDisplayRule
import com.novaco.luxapi.commons.config.gui.rule.ToggleDisplayRule
import com.novaco.luxapi.commons.config.type.ConfigItemStack
import com.novaco.luxapi.commons.event.player.PlayerQuitEvent
import com.novaco.luxapi.commons.gui.ClickType
import com.novaco.luxapi.commons.gui.Gui
import com.novaco.luxapi.commons.gui.GuiBuilder
import com.novaco.luxapi.commons.gui.GuiClickEvent
import com.novaco.luxapi.commons.gui.GuiItem
import com.novaco.luxapi.commons.gui.PaginatedGui
import com.novaco.luxapi.commons.gui.PaginatedGuiBuilder
import com.novaco.luxapi.commons.math.Vector3D
import com.novaco.luxapi.commons.player.LuxPlayer
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.UUID

private class QolFakePlayer(private val permissions: Set<String> = emptySet()) : LuxPlayer {
    override val uniqueId: UUID = UUID.randomUUID()
    override val parent: Any = Any()
    override val locale: String = "en_us"
    override val position: Vector3D = Vector3D(0.0, 0.0, 0.0)
    override val name: String = "Qol"
    val messages = mutableListOf<String>()

    override fun sendTitle(title: String, subtitle: String) {}
    override fun kick(reason: String) {}
    override fun sendMessage(message: String) { messages.add(message) }
    override fun hasPermission(permission: String): Boolean = permission in permissions
}

private class QolFakeGui : Gui {
    var closedFor: LuxPlayer? = null
    override fun open(player: LuxPlayer) {}
    override fun close(player: LuxPlayer) { closedFor = player }
    override fun setItem(slot: Int, item: GuiItem) {}
    override fun getItem(slot: Int): GuiItem? = null
    override fun refresh(player: LuxPlayer) {}
    override fun refreshAll() {}
    override fun hasViewers(): Boolean = true
}

private class RecordingGuiBuilder(private val log: MutableList<String>) : GuiBuilder() {
    override fun build(): Gui {
        log.add(title)
        return QolFakeGui()
    }
}

private class RecordingPaginatedGuiBuilder(private val log: MutableList<String>) : PaginatedGuiBuilder() {
    val setSlots = mutableMapOf<Int, GuiItem>()

    override fun setItem(slot: Int, item: GuiItem): PaginatedGuiBuilder {
        super.setItem(slot, item)
        setSlots[slot] = item
        return this
    }

    override fun build(): PaginatedGui {
        log.add(title)
        return object : PaginatedGui {
            override fun open(player: LuxPlayer) {}
            override fun close(player: LuxPlayer) {}
            override fun setItem(slot: Int, item: GuiItem) {}
            override fun getItem(slot: Int): GuiItem? = null
            override fun refresh(player: LuxPlayer) {}
            override fun refreshAll() {}
            override fun hasViewers(): Boolean = true
            override fun setPage(player: LuxPlayer, page: Int) {}
            override fun getCurrentPage(player: LuxPlayer): Int = 0
            override fun getTotalPages(): Int = 1
        }
    }
}

class GuiQolFeaturesTest {

    private val openLog = mutableListOf<String>()

    @BeforeEach
    fun setUp() {
        ConfigGuiRegistries.init()
        openLog.clear()
        LuxAPI.guiProvider = { RecordingGuiBuilder(openLog) }
        LuxAPI.paginatedGuiProvider = { RecordingPaginatedGuiBuilder(openLog) }
        ConfigGuiRegistries.commandDispatcher = null
        ConfigGuiRegistries.itemGiver = null
        ConfigGuiRegistries.soundPlayer = null
    }

    @Test
    fun `effectivePositions merges explicit positions and range shorthand, skipping malformed ranges`() {
        val item = ConfigMenuItem().apply {
            positions.add(0)
            positionRanges.add("10-12")
            positionRanges.add("not-a-range")
            positionRanges.add("5-3") // reversed, skipped
        }

        assertEquals(listOf(0, 10, 11, 12), item.effectivePositions())
    }

    @Test
    fun `animated item cycles frames on repeated calls, independent of which resolved item supplied them`() {
        val item = ConfigMenuItem().apply {
            type = "minecraft:stone"
            refreshTicks = 20
            frames.add(MenuItemFrame().apply { type = "minecraft:red_wool" })
            frames.add(MenuItemFrame().apply { type = "minecraft:blue_wool" })
        }
        val player = QolFakePlayer()

        assertTrue(item.isAnimated())
        assertEquals("minecraft:red_wool", item.nextFrameGuiItem(player)?.material)
        assertEquals("minecraft:blue_wool", item.nextFrameGuiItem(player)?.material)
        assertEquals("minecraft:red_wool", item.nextFrameGuiItem(player)?.material)
    }

    @Test
    fun `MenuNavigator open then back returns to the previous menu, refresh reopens the current one`() {
        val menuA = ConfigGuiInterface().apply { title = "Menu A" }
        val menuB = ConfigGuiInterface().apply { title = "Menu B" }
        MenuRegistry.register("a", menuA)
        MenuRegistry.register("b", menuB)

        val player = QolFakePlayer()
        MenuNavigator.open(player, "a")
        MenuNavigator.open(player, "b")
        MenuNavigator.refresh(player)
        MenuNavigator.back(player)

        assertEquals(listOf("Menu A", "Menu B", "Menu B", "Menu A"), openLog)
    }

    @Test
    fun `onOpen fires through MenuNavigator with a synthesized click event`() {
        val player = QolFakePlayer()
        val menu = ConfigGuiInterface().apply {
            title = "Welcome"
            onOpen.add(SendMessageClickAction().apply { message = "hello" })
        }
        MenuRegistry.register("welcome", menu)

        MenuNavigator.open(player, "welcome")

        assertEquals(listOf("hello"), player.messages)
    }

    @Test
    fun `OpenMenuClickAction and BackClickAction drive real navigation through a click event`() {
        val menuA = ConfigGuiInterface().apply { title = "Menu A" }
        val menuB = ConfigGuiInterface().apply { title = "Menu B" }
        MenuRegistry.register("nav-a", menuA)
        MenuRegistry.register("nav-b", menuB)

        val player = QolFakePlayer()
        MenuNavigator.open(player, "nav-a")
        openLog.clear()

        val event = GuiClickEvent(player, 0, ClickType.LEFT, QolFakeGui())
        assertTrue(OpenMenuClickAction().apply { menuId = "nav-b" }.handle(event))
        assertTrue(BackClickAction().handle(event))
        assertTrue(RefreshMenuClickAction().handle(event))

        assertEquals(listOf("Menu B", "Menu A", "Menu A"), openLog)
    }

    @Test
    fun `CooldownClickAction gates later actions and CooldownDisplayRule reflects the same cooldown`() {
        val player = QolFakePlayer()
        val onCooldownItem = ConfigMenuItem().apply { type = "minecraft:barrier" }
        val item = ConfigMenuItem().apply {
            type = "minecraft:chest"
            displayRules.add(CooldownDisplayRule().apply {
                cooldownId = "crate"
                this.onCooldownItem = onCooldownItem
            })
            // CooldownClickAction gates whatever comes after it; SendMessageClickAction here is
            // just a probe to prove whether the gated action ran.
            clickActions.add(CooldownClickAction().apply { cooldownId = "crate"; durationMillis = 60_000 })
            clickActions.add(SendMessageClickAction().apply { message = "opened" })
        }
        val event = GuiClickEvent(player, 0, ClickType.LEFT, QolFakeGui())

        assertEquals("minecraft:chest", item.resolve(player)?.type, "Not on cooldown yet — normal item shows.")

        item.toGuiItem(player)!!.clickHandler?.invoke(event)
        assertEquals(listOf("opened"), player.messages, "First click: not on cooldown, gate passes, message sent.")

        assertEquals("minecraft:barrier", item.resolve(player)?.type, "Now on cooldown — display rule swaps to onCooldownItem.")

        item.toGuiItem(player)!!.clickHandler?.invoke(event)
        assertEquals(listOf("opened"), player.messages, "Second click: gate blocks, no second message.")
    }

    @Test
    fun `PlaceholderConditionDisplayRule and ToggleDisplayRule swap items based on a resolved placeholder`() {
        PlaceholderManager.register(object : PlaceholderProvider {
            override fun identifier(): String = "qol"
            override fun onPlaceholderRequest(player: LuxPlayer?, params: String): String? =
                if (params == "state") "true" else null
        })
        val player = QolFakePlayer()

        val conditionItem = ConfigMenuItem().apply {
            type = "minecraft:emerald"
            displayRules.add(PlaceholderConditionDisplayRule().apply {
                placeholder = "%qol_state%"
                equals = "true"
                elseItem = ConfigMenuItem().apply { type = "minecraft:coal" }
            })
        }
        assertEquals("minecraft:emerald", conditionItem.resolve(player)?.type)

        val toggleItem = ConfigMenuItem().apply {
            displayRules.add(ToggleDisplayRule().apply {
                placeholder = "%qol_state%"
                trueItem = ConfigMenuItem().apply { type = "minecraft:lime_dye" }
                falseItem = ConfigMenuItem().apply { type = "minecraft:gray_dye" }
            })
        }
        assertEquals("minecraft:lime_dye", toggleItem.resolve(player)?.type)
    }

    @Test
    fun `GiveItemClickAction and SoundClickAction are documented no-ops until platform hooks are set, then work`() {
        val player = QolFakePlayer()
        val event = GuiClickEvent(player, 0, ClickType.LEFT, QolFakeGui())

        val giveAction = GiveItemClickAction().apply { item = ConfigItemStack().apply { type = "minecraft:apple"; amount = 3 } }
        val soundAction = SoundClickAction().apply { sound = "minecraft:entity.player.levelup" }

        assertTrue(giveAction.handle(event)) // no-op, no crash
        assertTrue(soundAction.handle(event))

        val given = mutableListOf<ConfigItemStack>()
        ConfigGuiRegistries.itemGiver = ItemGiver { _, item -> given.add(item) }
        val playedSounds = mutableListOf<String>()
        ConfigGuiRegistries.soundPlayer = SoundPlayer { _, sound, _, _ -> playedSounds.add(sound) }

        giveAction.handle(event)
        soundAction.handle(event)

        assertEquals(1, given.size)
        assertEquals("minecraft:apple", given[0].type)
        assertEquals(3, given[0].amount)
        assertEquals(listOf("minecraft:entity.player.levelup"), playedSounds)
    }

    @Test
    fun `a menu with an id self-registers on first populate, so OpenMenuClickAction can find it without a manual register call`() {
        val target = ConfigGuiInterface().apply { id = "self-reg"; title = "Self Registered" }
        val player = QolFakePlayer()

        // Shown once directly by application code — the one manual entry point any id-based
        // registry still needs — with no MenuRegistry.register() call anywhere.
        target.populate(LuxAPI.createMenu(), player)

        assertNotNull(MenuRegistry.get("self-reg"))

        val event = GuiClickEvent(player, 0, ClickType.LEFT, QolFakeGui())
        openLog.clear()
        assertTrue(OpenMenuClickAction().apply { menuId = "self-reg" }.handle(event))
        assertEquals(listOf("Self Registered"), openLog)
    }

    @Test
    fun `MenuNavigator clears a player's navigation state on quit`() {
        val menu = ConfigGuiInterface().apply { title = "Quit Test" }
        MenuRegistry.register("quit-test", menu)
        val player = QolFakePlayer()

        MenuNavigator.open(player, "quit-test")
        MenuNavigator.onPlayerQuit(PlayerQuitEvent(player))

        // No prior menu tracked anymore, so refresh (which needs a "current" menu) is a no-op.
        openLog.clear()
        MenuNavigator.refresh(player)
        assertTrue(openLog.isEmpty())
    }
}
