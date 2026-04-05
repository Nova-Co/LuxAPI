package com.novaco.luxapi.neoforge.event

import com.novaco.luxapi.commons.event.EventBus
import com.novaco.luxapi.commons.event.Subscribe
import com.novaco.luxapi.commons.event.player.PlayerChatEvent
import com.novaco.luxapi.commons.event.player.PlayerJoinEvent
import com.novaco.luxapi.commons.event.player.PlayerQuitEvent
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

/**
 * A dummy listener to intercept and verify events passing through the cross-platform EventBus.
 */
class TestBridgeSubscriber {
    var joinFired = false
    var quitFired = false
    var chatEvent: PlayerChatEvent? = null

    @Subscribe
    fun onJoin(event: PlayerJoinEvent) { joinFired = true }

    @Subscribe
    fun onQuit(event: PlayerQuitEvent) { quitFired = true }

    @Subscribe
    fun onChat(event: PlayerChatEvent) { chatEvent = event }
}

class NeoForgeEventBridgeTest {

    private lateinit var testSubscriber: TestBridgeSubscriber

    companion object {
        @JvmStatic
        @BeforeAll
        fun initBridge() {
            SharedConstants.tryDetectVersion()
            Bootstrap.bootStrap()
        }
    }

    @BeforeEach
    fun setup() {
        testSubscriber = TestBridgeSubscriber()
        EventBus.register(testSubscriber)
    }

    @AfterEach
    fun teardown() {
        EventBus.unregister(testSubscriber)
    }

    @Test
    fun `test neoforge login event translates to lux join event`() {
        // 1. Mock the native ServerPlayer and the Forge Event
        val mockPlayer = mock<ServerPlayer>()
        val mockEvent = mock<PlayerEvent.PlayerLoggedInEvent>()

        whenever(mockEvent.entity).thenReturn(mockPlayer)

        // 2. Pass it directly to the bridge handler
        NeoForgeEventBridge.onPlayerJoin(mockEvent)

        // 3. Verify our EventBus caught the translated event
        assertTrue(testSubscriber.joinFired, "The native PlayerLoggedInEvent should trigger PlayerJoinEvent on the EventBus.")
    }

    @Test
    fun `test neoforge logout event translates to lux quit event`() {
        val mockPlayer = mock<ServerPlayer>()
        val mockEvent = mock<PlayerEvent.PlayerLoggedOutEvent>()

        whenever(mockEvent.entity).thenReturn(mockPlayer)

        NeoForgeEventBridge.onPlayerQuit(mockEvent)

        assertTrue(testSubscriber.quitFired, "The native PlayerLoggedOutEvent should trigger PlayerQuitEvent on the EventBus.")
    }

    @Test
    @Disabled("Requires a full NeoForge Server environment due to intermediary field obfuscation (field_xxx) blocking Mockito.")
    fun `test neoforge chat event translates, formats, and broadcasts correctly`() {
        val mockServer = mock<MinecraftServer>()
        val mockPlayerList = mock<PlayerList>()
        val mockSender = mock<ServerPlayer>()
        val mockTargetPlayer = mock<ServerPlayer>()
        val mockEvent = mock<ServerChatEvent>()

        // Use Reflection to set the public 'server' field on the sender
        val serverField = ServerPlayer::class.java.getDeclaredField("server")
        serverField.isAccessible = true
        serverField.set(mockSender, mockServer)

        // Use Reflection to set the public 'players' list on the PlayerList
        val playersField = PlayerList::class.java.getDeclaredField("players")
        playersField.isAccessible = true
        playersField.set(mockPlayerList, listOf(mockSender, mockTargetPlayer))

        whenever(mockServer.playerList).thenReturn(mockPlayerList)
        whenever(mockSender.scoreboardName).thenReturn("Hero")
        whenever(mockEvent.player).thenReturn(mockSender)
        whenever(mockEvent.rawText).thenReturn("Hello NeoForge!")

        // Manually trigger the event
        NeoForgeEventBridge.onPlayerChat(mockEvent)

        // Verify Event Translation
        assertNotNull(testSubscriber.chatEvent, "The native Chat event should trigger PlayerChatEvent.")
        assertEquals("Hello NeoForge!", testSubscriber.chatEvent?.message)
        assertEquals(2, testSubscriber.chatEvent?.recipients?.size, "Recipients list should contain all online players.")

        // Verify native event cancellation (to block vanilla chat)
        verify(mockEvent).isCanceled = true

        // Verify custom component delivery to targets
        verify(mockTargetPlayer).sendSystemMessage(any<Component>())
    }

    @Test
    @Disabled("Requires a full NeoForge Server environment due to intermediary field obfuscation (field_xxx) blocking Mockito.")
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

        // Intercept and cancel the event before it broadcasts
        val cancelSubscriber = object {
            @Subscribe
            fun onChat(event: PlayerChatEvent) {
                event.isCancelled = true
            }
        }
        EventBus.register(cancelSubscriber)

        // Manually trigger the event
        NeoForgeEventBridge.onPlayerChat(mockEvent)

        EventBus.unregister(cancelSubscriber)

        // Verify the native event was still canceled
        verify(mockEvent).isCanceled = true

        // Verify NO manual messages were sent because our cross-platform event was cancelled
        verify(mockSender, never()).sendSystemMessage(any<Component>())
    }
}