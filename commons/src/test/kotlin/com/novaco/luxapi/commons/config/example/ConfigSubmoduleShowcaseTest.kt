package com.novaco.luxapi.commons.config.example

import com.novaco.luxapi.commons.config.ConfigService
import com.novaco.luxapi.commons.config.LuxConfig
import com.novaco.luxapi.commons.config.annotation.Comment
import com.novaco.luxapi.commons.config.annotation.Config
import com.novaco.luxapi.commons.config.annotation.ScalarSerializers
import com.novaco.luxapi.commons.config.exception.ConfigException
import com.novaco.luxapi.commons.config.gui.CommandDispatcher
import com.novaco.luxapi.commons.config.gui.ConfigGuiInterface
import com.novaco.luxapi.commons.config.gui.ConfigGuiRegistries
import com.novaco.luxapi.commons.config.gui.ConfigMenuItem
import com.novaco.luxapi.commons.config.gui.PaginatedConfigGuiInterface
import com.novaco.luxapi.commons.config.gui.action.CloseMenuClickAction
import com.novaco.luxapi.commons.config.gui.action.ExecuteCommandsClickAction
import com.novaco.luxapi.commons.config.gui.rule.RequiresPermissionDisplayRule
import com.novaco.luxapi.commons.config.serializer.ConfigTypeSerializerRegistry
import com.novaco.luxapi.commons.config.serializer.PatternTypeSerializer
import com.novaco.luxapi.commons.config.serializer.PolymorphicConfigEntry
import com.novaco.luxapi.commons.config.serializer.PolymorphicConfigRegistry
import com.novaco.luxapi.commons.config.type.ConfigLocation
import com.novaco.luxapi.commons.config.type.ConfigRandomWeightedSet
import com.novaco.luxapi.commons.config.type.DateFormatConfig
import com.novaco.luxapi.commons.config.type.ProgressBar
import com.novaco.luxapi.commons.config.type.TimeFormatConfig
import com.novaco.luxapi.commons.config.util.ConfigUtils
import com.novaco.luxapi.commons.chat.placeholder.PlaceholderManager
import com.novaco.luxapi.commons.chat.placeholder.PlaceholderProvider
import com.novaco.luxapi.commons.gui.Gui
import com.novaco.luxapi.commons.gui.GuiBuilder
import com.novaco.luxapi.commons.gui.GuiItem
import com.novaco.luxapi.commons.gui.PaginatedGui
import com.novaco.luxapi.commons.gui.PaginatedGuiBuilder
import com.novaco.luxapi.commons.math.Vector3D
import com.novaco.luxapi.commons.player.LuxPlayer
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.spongepowered.configurate.BasicConfigurationNode
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import java.io.File
import java.util.UUID
import java.util.regex.Pattern

/**
 * A worked example — a "Daily Rewards" server module — touching every public class in
 * `commons/config`, end to end: base config lifecycle, custom/polymorphic serializers, the
 * portable value types, and the config-driven GUI layer. Written as a runnable test so
 * "it compiles and passes" is proof the coverage claim actually holds.
 */

// ---------------------------------------------------------------------------------------------
// Reward tiers: PolymorphicConfigEntry / PolymorphicConfigRegistry used outside the GUI layer,
// showing the mechanism is general-purpose, not GUI-specific.
// ---------------------------------------------------------------------------------------------

interface RewardTier : PolymorphicConfigEntry {
    fun describe(): String
}

@ConfigSerializable
class ItemReward : RewardTier {
    var itemId: String = "minecraft:apple"
    var amount: Int = 1
    override fun id(): String = "item"
    override fun describe(): String = "$amount x $itemId"
}

@ConfigSerializable
class CommandReward : RewardTier {
    var command: String = "say hello"
    override fun id(): String = "command"
    override fun describe(): String = "run: $command"
}

object RewardRegistries {
    val tiers = PolymorphicConfigRegistry<RewardTier>()

    @Synchronized
    fun init() {
        if (tiers.get("item") != null) return
        tiers.register("item", ItemReward::class.java)
        tiers.register("command", CommandReward::class.java)
        ConfigTypeSerializerRegistry.register(RewardTier::class.java, tiers.serializer(RewardTier::class.java))
    }
}

// ---------------------------------------------------------------------------------------------
// Main plugin config: LuxConfig, ConfigService, @Config(resource=), @Comment, @ScalarSerializers,
// ConfigLocation, TimeFormatConfig, DateFormatConfig, ProgressBar, ConfigRandomWeightedSet,
// PatternTypeSerializer, and the polymorphic RewardTier list above.
// ---------------------------------------------------------------------------------------------

@ConfigSerializable
@Config("daily-rewards.yml", resource = "daily-rewards.yml")
@ScalarSerializers([PatternTypeSerializer::class])
@Comment("Daily rewards module configuration.")
class DailyRewardsConfig : LuxConfig() {

    @Comment("Where the daily rewards NPC spawns.")
    var spawnLocation: ConfigLocation = ConfigLocation.builder()
        .worldName("world").x(0.0).y(64.0).z(0.0).build()

    @Comment("How claim cooldowns are displayed to players.")
    var cooldownDisplay: TimeFormatConfig = TimeFormatConfig.of(TimeFormatConfig.Style.SHORT)

    @Comment("Format for the event end date shown in the menu.")
    var eventEndDate: DateFormatConfig = DateFormatConfig.of("dd MMM yyyy")

    @Comment("Claim progress bar shown in chat.")
    var claimProgressBar: ProgressBar = ProgressBar().apply { length = 20 }

    @Comment("Weighted loot pool for the mystery reward tier.")
    var lootPool: ConfigRandomWeightedSet<String> = ConfigRandomWeightedSet<String>().apply {
        add("minecraft:diamond", 1.0)
        add("minecraft:iron_ingot", 5.0)
    }

    @Comment("Only usernames matching this pattern may claim (regex).")
    var usernameFilter: Pattern = Pattern.compile("^[a-zA-Z0-9_]{3,16}$")

    var rewardTiers: MutableList<RewardTier> = mutableListOf(ItemReward(), CommandReward())
}

// ---------------------------------------------------------------------------------------------
// Per-arena configs: LuxConfig + ConfigService.loadAll() over a directory.
// ---------------------------------------------------------------------------------------------

@ConfigSerializable
@Config("arena.yml")
class ArenaConfig : LuxConfig() {
    var name: String = "unnamed"
    var location: ConfigLocation = ConfigLocation()
}

// ---------------------------------------------------------------------------------------------
// Config-driven menu: PaginatedConfigGuiInterface, ConfigMenuItem, ClickAction/ItemDisplayRule
// built-ins (CloseMenuClickAction, ExecuteCommandsClickAction, RequiresPermissionDisplayRule).
// ---------------------------------------------------------------------------------------------

fun buildDailyRewardsMenuConfig(): PaginatedConfigGuiInterface = PaginatedConfigGuiInterface().apply {
    title = "Daily Rewards"
    rows = 3
    fillType = ConfigGuiInterface.FillType.BLOCK
    filler = ConfigMenuItem().apply {
        type = "minecraft:gray_stained_glass_pane"
        name = " "
    }

    items.add(ConfigMenuItem().apply {
        type = "minecraft:chest"
        name = "&aClaim Reward"
        positions.add(13)
        displayRules.add(RequiresPermissionDisplayRule().apply {
            permission = "luxapi.dailyrewards.claim"
            elseItem = ConfigMenuItem().apply {
                type = "minecraft:barrier"
                name = "&cNo permission"
                positions.add(13)
            }
        })
        clickActions.add(ExecuteCommandsClickAction().apply {
            commands.add("dailyrewards claim %player_name%")
        })
    })

    items.add(ConfigMenuItem().apply {
        type = "minecraft:barrier"
        name = "&cClose"
        positions.add(22)
        clickActions.add(CloseMenuClickAction())
    })
}

// ---------------------------------------------------------------------------------------------
// One config FILE, every class: everything above is spread across three config classes for
// clarity. HubConfig instead embeds every commons/config type as fields on a single LuxConfig,
// menu included — loading it produces exactly one hub.yml with every piece in it.
// ---------------------------------------------------------------------------------------------

@ConfigSerializable
@Config("hub.yml")
@ScalarSerializers([PatternTypeSerializer::class])
@Comment("Central hub configuration: spawn, rewards, and menu, all in one file.")
class HubConfig : LuxConfig() {

    @Comment("Where players spawn when they join the hub.")
    var spawnLocation: ConfigLocation = ConfigLocation.builder()
        .worldName("hub").x(0.5).y(100.0).z(0.5).build()

    @Comment("How claim cooldowns are displayed.")
    var cooldownDisplay: TimeFormatConfig = TimeFormatConfig.of(TimeFormatConfig.Style.LONG)

    @Comment("Format for the seasonal event end date.")
    var eventEndDate: DateFormatConfig = DateFormatConfig.of("dd MMM yyyy")

    @Comment("XP progress bar shown in chat.")
    var xpBar: ProgressBar = ProgressBar().apply {
        length = 20
        filledChar = '|'
        emptyChar = '.'
    }

    @Comment("Weighted mystery loot pool for the hub crate.")
    var lootPool: ConfigRandomWeightedSet<String> = ConfigRandomWeightedSet<String>().apply {
        add("minecraft:diamond", 1.0)
        add("minecraft:iron_ingot", 5.0)
        add("minecraft:apple", 20.0)
    }

    @Comment("Only usernames matching this pattern may join hub events.")
    var usernameFilter: Pattern = Pattern.compile("^[a-zA-Z0-9_]{3,16}$")

    @Comment("Reward tiers granted by the hub crate — polymorphic: item or command.")
    var rewardTiers: MutableList<RewardTier> = mutableListOf(ItemReward(), CommandReward())

    @Comment("The hub menu, entirely config-driven, embedded in this same file.")
    var menu: PaginatedConfigGuiInterface = PaginatedConfigGuiInterface().apply {
        title = "Hub Menu"
        rows = 3
        fillType = ConfigGuiInterface.FillType.CHECKERED
        filler = ConfigMenuItem().apply {
            type = "minecraft:gray_stained_glass_pane"
            name = " "
        }

        items.add(ConfigMenuItem().apply {
            type = "minecraft:chest"
            name = "&aOpen Crate"
            positions.add(13)
            displayRules.add(RequiresPermissionDisplayRule().apply {
                permission = "luxapi.hub.crate"
                elseItem = ConfigMenuItem().apply {
                    type = "minecraft:barrier"
                    name = "&cLocked"
                    positions.add(13)
                }
            })
            clickActions.add(ExecuteCommandsClickAction().apply {
                commands.add("hub crate open %player_name%")
            })
        })

        items.add(ConfigMenuItem().apply {
            type = "minecraft:barrier"
            name = "&cClose"
            positions.add(22)
            clickActions.add(CloseMenuClickAction())
        })
    }
}

// ---------------------------------------------------------------------------------------------
// Minimal platform-side stand-ins — a real plugin would use its Bukkit/Fabric/NeoForge module's
// own GuiBuilder/Gui/LuxPlayer implementations; these just let this example run standalone.
// ---------------------------------------------------------------------------------------------

private class FakeLuxPlayer(private val permissions: Set<String> = emptySet()) : LuxPlayer {
    override val uniqueId: UUID = UUID.randomUUID()
    override val parent: Any = Any()
    override val locale: String = "en_us"
    override val position: Vector3D = Vector3D(0.0, 0.0, 0.0)
    override val name: String = "Showcase"

    override fun sendTitle(title: String, subtitle: String) {}
    override fun kick(reason: String) {}
    override fun sendMessage(message: String) {}
    override fun hasPermission(permission: String): Boolean = permission in permissions
}

private class RecordingPaginatedGuiBuilder : PaginatedGuiBuilder() {
    val setSlots = mutableMapOf<Int, GuiItem>()

    override fun setItem(slot: Int, item: GuiItem): PaginatedGuiBuilder {
        super.setItem(slot, item)
        setSlots[slot] = item
        return this
    }

    override fun build(): PaginatedGui = object : PaginatedGui {
        override fun open(player: LuxPlayer) {}
        override fun close(player: LuxPlayer) {}
        override fun setItem(slot: Int, item: GuiItem) {}
        override fun getItem(slot: Int): GuiItem? = null
        override fun refresh(player: LuxPlayer) {}
        override fun refreshAll() {}
        override fun hasViewers(): Boolean = true
        override val generation: Int = 0
        override fun setPage(player: LuxPlayer, page: Int) {}
        override fun getCurrentPage(player: LuxPlayer): Int = 0
        override fun getTotalPages(): Int = 1
    }
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

class ConfigSubmoduleShowcaseTest {

    @Test
    fun `daily rewards config loads the bundled default resource and every value type round-trips`(@TempDir tempDir: File) {
        RewardRegistries.init()

        val config = ConfigService.load(DailyRewardsConfig::class.java, tempDir)

        // @Config(resource = ...) copied the bundled default in on first load.
        assertEquals("bundled-world", config.spawnLocation.worldName)
        assertEquals(TimeFormatConfig.Style.DIGITAL, config.cooldownDisplay.style)
        assertEquals("1970-01-01", config.eventEndDate.format(0L)) // pattern from the bundled resource: "yyyy-MM-dd"

        assertEquals(Vector3D(100.0, 70.0, 100.0), config.spawnLocation.toVector3D())
        assertTrue(config.usernameFilter.matcher("lux_player1").matches())
        assertFalse(config.usernameFilter.matcher("!!invalid!!").matches())
        assertTrue(config.claimProgressBar.render(0.5).isNotEmpty())
        assertNotNull(config.lootPool.getRandom())

        // Polymorphic RewardTier list survived a save/reload round trip.
        config.save()
        val reloaded = ConfigService.load(DailyRewardsConfig::class.java, tempDir)
        assertEquals(setOf("item", "command"), reloaded.rewardTiers.map { it.id() }.toSet())

        // @ScalarSerializers(PatternTypeSerializer) round-tripped the regex, class-scoped.
        assertEquals(config.usernameFilter.pattern(), reloaded.usernameFilter.pattern())
    }

    @Test
    fun `unannotated class and malformed yaml surface the right exception types`(@TempDir tempDir: File) {
        class NotAConfig : LuxConfig()

        assertThrows(IllegalArgumentException::class.java) {
            ConfigService.load(NotAConfig::class.java, tempDir)
        }

        File(tempDir, "arena.yml").writeText("name: [unterminated\n")
        assertThrows(ConfigException::class.java) {
            ConfigService.load(ArenaConfig::class.java, tempDir)
        }
    }

    @Test
    fun `loadAll reads a directory of arena configs`(@TempDir tempDir: File) {
        val arenasDir = File(tempDir, "arenas").apply { mkdirs() }
        File(arenasDir, "spawn.yml").writeText("name: spawn\nlocation:\n  worldName: overworld\n")
        File(arenasDir, "nether.yml").writeText("name: nether\nlocation:\n  worldName: the_nether\n")

        val arenas = ConfigService.loadAll(ArenaConfig::class.java, arenasDir)

        assertEquals(setOf("spawn", "nether"), arenas.map { it.name }.toSet())
    }

    @Test
    fun `ConfigUtils getList degrades gracefully instead of throwing on bad data`() {
        val node = BasicConfigurationNode.root().node("bonus-days").set(listOf(1, 2, 3))
        val good = ConfigUtils.getList(node.parent()!!, Int::class.javaObjectType, "bonus-days")
        assertEquals(listOf(1, 2, 3), good)

        val badNode = BasicConfigurationNode.root().node("bonus-days").set("not-a-list")
        val bad = ConfigUtils.getList(badNode.parent()!!, Int::class.javaObjectType, "bonus-days")
        assertEquals(emptyList<Int>(), bad)
    }

    @Test
    fun `config-driven menu resolves permissions and populates a platform GuiBuilder`() {
        ConfigGuiRegistries.init()
        PlaceholderManager.register(object : PlaceholderProvider {
            override fun identifier(): String = "player"
            override fun onPlaceholderRequest(player: LuxPlayer?, params: String): String? =
                if (params == "name") player?.name else null
        })
        val dispatched = mutableListOf<String>()
        ConfigGuiRegistries.commandDispatcher = CommandDispatcher { _, command -> dispatched.add(command) }

        val menu = buildDailyRewardsMenuConfig()

        val withPermission = FakeLuxPlayer(setOf("luxapi.dailyrewards.claim"))
        val withoutPermission = FakeLuxPlayer()

        val allowedBuilder = RecordingPaginatedGuiBuilder()
        menu.populatePaginated(allowedBuilder, withPermission)
        assertEquals("&aClaim Reward", allowedBuilder.setSlots[13]?.displayName)

        val deniedBuilder = RecordingPaginatedGuiBuilder()
        menu.populatePaginated(deniedBuilder, withoutPermission)
        assertEquals("&cNo permission", deniedBuilder.setSlots[13]?.displayName)

        // Filler covers every slot the two declared items didn't claim (BLOCK fill).
        assertEquals(27, allowedBuilder.setSlots.size)

        // ExecuteCommandsClickAction dispatches through the platform-supplied CommandDispatcher.
        val gui = FakeGui()
        val claimItem = allowedBuilder.setSlots[13]!!
        val fakeEvent = com.novaco.luxapi.commons.gui.GuiClickEvent(
            withPermission, 13, com.novaco.luxapi.commons.gui.ClickType.LEFT, gui
        )
        claimItem.clickHandler?.invoke(fakeEvent)
        assertEquals(listOf("dailyrewards claim Showcase"), dispatched)

        // CloseMenuClickAction closes the gui for the clicking player.
        val closeItem = allowedBuilder.setSlots[22]!!
        closeItem.clickHandler?.invoke(fakeEvent.copy(slot = 22))
        assertEquals(withPermission, gui.closedFor)
    }

    @Test
    fun `a single HubConfig file exercises every class in commons config`(@TempDir tempDir: File) {
        RewardRegistries.init()
        ConfigGuiRegistries.init()
        PlaceholderManager.register(object : PlaceholderProvider {
            override fun identifier(): String = "player"
            override fun onPlaceholderRequest(player: LuxPlayer?, params: String): String? =
                if (params == "name") player?.name else null
        })
        val dispatched = mutableListOf<String>()
        ConfigGuiRegistries.commandDispatcher = CommandDispatcher { _, command -> dispatched.add(command) }

        val hub = ConfigService.load(HubConfig::class.java, tempDir)

        // Every nested value type is populated and usable straight off the loaded instance.
        assertEquals("hub", hub.spawnLocation.worldName)
        assertEquals(Vector3D(0.5, 100.0, 0.5), hub.spawnLocation.toVector3D())
        assertTrue(hub.cooldownDisplay.format(java.time.Duration.ofDays(1)).contains("day"))
        assertTrue(hub.xpBar.render(0.5).isNotEmpty())
        assertNotNull(hub.lootPool.getRandom())
        assertTrue(hub.usernameFilter.matcher("lux_player").matches())
        assertFalse(hub.usernameFilter.matcher("!!bad!!").matches())
        assertEquals(setOf("item", "command"), hub.rewardTiers.map { it.id() }.toSet())

        // The embedded menu, the polymorphic reward list, the regex, and the value types all
        // round-trip through a save/reload of this ONE file.
        hub.save()
        val reloaded = ConfigService.load(HubConfig::class.java, tempDir)
        assertEquals(2, reloaded.menu.items.size)
        assertEquals("&aOpen Crate", reloaded.menu.items[0].name)
        assertEquals(hub.usernameFilter.pattern(), reloaded.usernameFilter.pattern())
        assertEquals(setOf("item", "command"), reloaded.rewardTiers.map { it.id() }.toSet())

        // The embedded menu is fully live off the reloaded instance: permission gating, filler
        // pattern, and click dispatch all work exactly as if it were its own config file.
        val withPermission = FakeLuxPlayer(setOf("luxapi.hub.crate"))
        val builder = RecordingPaginatedGuiBuilder()
        reloaded.menu.populatePaginated(builder, withPermission)
        assertEquals("&aOpen Crate", builder.setSlots[13]?.displayName)
        assertEquals("&cClose", builder.setSlots[22]?.displayName)
        // CHECKERED fill only covers alternating slots, not every empty one (unlike BLOCK) —
        // proof the fillType actually changes behavior, not just accepted as a no-op value.
        assertTrue(builder.setSlots.size in 3 until 27)

        val gui = FakeGui()
        val event = com.novaco.luxapi.commons.gui.GuiClickEvent(
            withPermission, 13, com.novaco.luxapi.commons.gui.ClickType.LEFT, gui
        )
        builder.setSlots[13]!!.clickHandler?.invoke(event)
        assertEquals(listOf("hub crate open Showcase"), dispatched)

        // A corrupted hub.yml surfaces ConfigException, not a raw Configurate exception.
        File(tempDir, "hub.yml").writeText("spawnLocation: [broken\n")
        assertThrows(ConfigException::class.java) {
            ConfigService.load(HubConfig::class.java, tempDir)
        }
    }
}
