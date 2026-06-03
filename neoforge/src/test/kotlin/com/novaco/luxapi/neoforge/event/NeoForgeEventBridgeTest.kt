package com.novaco.luxapi.neoforge.event

import com.novaco.luxapi.commons.event.EventBus
import com.novaco.luxapi.commons.event.Subscribe
import com.novaco.luxapi.commons.event.player.PlayerChatEvent
import com.novaco.luxapi.commons.event.player.PlayerJoinEvent
import com.novaco.luxapi.commons.event.player.PlayerQuitEvent
import com.novaco.luxapi.neoforge.LuxNeoForgeInitializer
import com.novaco.luxapi.neoforge.player.NeoForgeLuxPlayer
import com.novaco.luxapi.neoforge.player.NeoForgePlayerManager
import net.minecraft.SharedConstants
import net.minecraft.network.chat.Component
import net.minecraft.server.Bootstrap
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.players.PlayerList
import net.neoforged.neoforge.event.ServerChatEvent
import net.neoforged.neoforge.event.entity.player.PlayerEvent
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*
import java.util.UUID

/**
 * A dummy listener to intercept and verify events passing through the cross-platform EventBus.
 */
class TestBridgeSubscriber {
    var joinFired = false
    var quitFired = false
    var chatEvent: PlayerChatEvent? = null

    /**
     * Intercepts the join event.
     */
    @Subscribe
    fun onJoin(event: PlayerJoinEvent) { joinFired = true }

    /**
     * Intercepts the quit event.
     */
    @Subscribe
    fun onQuit(event: PlayerQuitEvent) { quitFired = true }

    /**
     * Intercepts the chat event.
     */
    @Subscribe
    fun onChat(event: PlayerChatEvent) { chatEvent = event }
}

/**
 * Unit tests evaluating the translation of native NeoForge events into cross-platform LuxAPI events.
 */
class NeoForgeEventBridgeTest {

    private lateinit var testSubscriber: TestBridgeSubscriber

    companion object {
        /**
         * Initializes the server registries.
         */
        @JvmStatic
        @BeforeAll
        fun initBridge() {
            SharedConstants.tryDetectVersion()
            Bootstrap.bootStrap()
        }
    }

    /**
     * Sets up the event subscribers and injects a mock PlayerManager.
     */
    @BeforeEach
    fun setup() {
        testSubscriber = TestBridgeSubscriber()
        EventBus.register(testSubscriber)

        val mockManager = mock<NeoForgePlayerManager>()
        val field = LuxNeoForgeInitializer::class.java.getDeclaredField("playerManager")
        field.isAccessible = true
        field.set(null, mockManager)

        whenever(mockManager.login(any())).thenAnswer { NeoForgeLuxPlayer(it.getArgument(0)) }
        whenever(mockManager.getPlayer(any<UUID>())).thenAnswer {
            val p = mock<ServerPlayer>()
            whenever(p.uuid).thenReturn(it.getArgument(0))
            NeoForgeLuxPlayer(p)
        }
    }

    /**
     * Unregisters the subscriber to ensure a clean slate.
     */
    @AfterEach
    fun teardown() {
        EventBus.unregister(testSubscriber)
    }

    /**
     * Verifies that the native JOIN event successfully delegates to the cross-platform PlayerJoinEvent.
     */
    @Test
    fun `test neoforge login event translates to lux join event`() {
        val mockPlayer = mock<ServerPlayer>()
        val mockEvent = mock<PlayerEvent.PlayerLoggedInEvent>()

        whenever(mockEvent.entity).thenReturn(mockPlayer)

        NeoForgeEventBridge.onPlayerJoin(mockEvent)

        assertTrue(testSubscriber.joinFired)
    }

    /**
     * Verifies that the native DISCONNECT event successfully delegates to the cross-platform PlayerQuitEvent.
     */
    @Test
    fun `test neoforge logout event translates to lux quit event`() {
        val mockPlayer = mock<ServerPlayer>()
        val mockEvent = mock<PlayerEvent.PlayerLoggedOutEvent>()

        whenever(mockPlayer.uuid).thenReturn(UUID.randomUUID())
        whenever(mockEvent.entity).thenReturn(mockPlayer)

        NeoForgeEventBridge.onPlayerQuit(mockEvent)

        assertTrue(testSubscriber.quitFired)
    }

    /**
     * Validates that standard chat messages are caught, formatted, and delivered correctly.
     */
    @Test
    @Disabled("Requires a full NeoForge Server environment due to intermediary field obfuscation blocking Mockito.")
    fun `test neoforge chat event translates formats and broadcasts correctly`() {
        val mockServer = mock<MinecraftServer>()
        val mockPlayerList = mock<PlayerList>()
        val mockSender = mock<ServerPlayer>()
        val mockTargetPlayer = mock<ServerPlayer>()
        val mockEvent = mock<ServerChatEvent>()

        val serverField = ServerPlayer::class.java.getDeclaredField("server")
        serverField.isAccessible = true
        serverField.set(mockSender, mockServer)

        val playersField = PlayerList::class.java.getDeclaredField("players")
        playersField.isAccessible = true
        playersField.set(mockPlayerList, listOf(mockSender, mockTargetPlayer))

        whenever(mockServer.playerList).thenReturn(mockPlayerList)
        whenever(mockSender.scoreboardName).thenReturn("Hero")
        whenever(mockEvent.player).thenReturn(mockSender)
        whenever(mockEvent.rawText).thenReturn("Hello NeoForge!")

        NeoForgeEventBridge.onPlayerChat(mockEvent)

        assertNotNull(testSubscriber.chatEvent)
        assertEquals("Hello NeoForge!", testSubscriber.chatEvent?.message)
        assertEquals(2, testSubscriber.chatEvent?.recipients?.size)
        verify(mockEvent).isCanceled = true
        verify(mockTargetPlayer).sendSystemMessage(any<Component>())
    }

    /**
     * Ensures that cancelling the cross-platform chat event halts native delivery.
     */
    @Test
    @Disabled("Requires a full NeoForge Server environment due to intermediary field obfuscation blocking Mockito.")
    fun `test cancelled lux chat event blocks delivery`() {
        val mockServer = mock<MinecraftServer>()
        val mockPlayerList = mock<PlayerList>()
        val mockSender = mock<ServerPlayer>()
        val mockEvent = mock<ServerChatEvent>()

        val serverField = ServerPlayer::class.java.getDeclaredField("server")
        serverField.isAccessible = true
        serverField.set(mockSender, mockServer)

        val playersField = PlayerList::class.java.getDeclaredField("players")
        playersField.isAccessible = true
        playersField.set(mockPlayerList, listOf(mockSender))

        whenever(mockServer.playerList).thenReturn(mockPlayerList)
        whenever(mockEvent.player).thenReturn(mockSender)
        whenever(mockEvent.rawText).thenReturn("Bad Words")

        val cancelSubscriber = object {
            @Subscribe
            fun onChat(event: PlayerChatEvent) {
                event.isCancelled = true
            }
        }
        EventBus.register(cancelSubscriber)

        NeoForgeEventBridge.onPlayerChat(mockEvent)

        EventBus.unregister(cancelSubscriber)

        verify(mockEvent).isCanceled = true
        verify(mockSender, never()).sendSystemMessage(any<Component>())
    }
}