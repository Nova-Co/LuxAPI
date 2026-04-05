package com.novaco.luxapi.fabric.event

import com.novaco.luxapi.commons.event.EventBus
import com.novaco.luxapi.commons.event.Subscribe
import com.novaco.luxapi.commons.event.player.PlayerChatEvent
import com.novaco.luxapi.commons.event.player.PlayerJoinEvent
import com.novaco.luxapi.commons.event.player.PlayerQuitEvent
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents
import net.fabricmc.fabric.api.networking.v1.PacketSender
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents
import net.minecraft.SharedConstants
import net.minecraft.network.chat.ChatType
import net.minecraft.network.chat.PlayerChatMessage
import net.minecraft.server.Bootstrap
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.network.ServerGamePacketListenerImpl
import net.minecraft.server.players.PlayerList
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

class FabricEventBridgeTest {

    private lateinit var testSubscriber: TestBridgeSubscriber

    companion object {
        @JvmStatic
        @BeforeAll
        fun initBridge() {
            SharedConstants.tryDetectVersion()
            Bootstrap.bootStrap()

            // Register the bridge only once for the entire test suite
            FabricEventBridge.register()
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
    fun `test fabric join event translates to lux join event`() {
        val mockHandler = mock<ServerGamePacketListenerImpl>()
        val mockPlayer = mock<ServerPlayer>()
        val mockSender = mock<PacketSender>()
        val mockServer = mock<MinecraftServer>()

        // Use Reflection to safely set the public 'player' field
        val playerField = ServerGamePacketListenerImpl::class.java.getField("player")
        playerField.isAccessible = true
        playerField.set(mockHandler, mockPlayer)

        ServerPlayConnectionEvents.JOIN.invoker().onPlayReady(mockHandler, mockSender, mockServer)

        assertTrue(testSubscriber.joinFired, "The native JOIN event should trigger PlayerJoinEvent on the EventBus.")
    }

    @Test
    fun `test fabric disconnect event translates to lux quit event`() {
        val mockHandler = mock<ServerGamePacketListenerImpl>()
        val mockPlayer = mock<ServerPlayer>()
        val mockServer = mock<MinecraftServer>()

        // Use Reflection to safely set the public 'player' field
        val playerField = ServerGamePacketListenerImpl::class.java.getField("player")
        playerField.isAccessible = true
        playerField.set(mockHandler, mockPlayer)

        ServerPlayConnectionEvents.DISCONNECT.invoker().onPlayDisconnect(mockHandler, mockServer)

        assertTrue(testSubscriber.quitFired, "The native DISCONNECT event should trigger PlayerQuitEvent on the EventBus.")
    }

    @Test
    @Disabled("Requires a full Fabric Server environment due to intermediary field obfuscation (field_xxx) blocking Mockito.")
    fun `test fabric chat event translates, formats, and broadcasts correctly`() {
        val mockServer = mock<MinecraftServer>()
        val mockPlayerList = mock<PlayerList>()
        val mockSender = mock<ServerPlayer>()
        val mockMessage = mock<PlayerChatMessage>()
        val mockChatParams = mock<ChatType.Bound>()

        // This fails in unit tests because 'server' is obfuscated to 'field_xxx' at runtime
        whenever(mockSender.server).thenReturn(mockServer)
        whenever(mockServer.playerList).thenReturn(mockPlayerList)
        whenever(mockSender.scoreboardName).thenReturn("Hero")
        whenever(mockMessage.signedContent()).thenReturn("Hello Server!")

        val isVanillaBroadcastAllowed = ServerMessageEvents.ALLOW_CHAT_MESSAGE.invoker().allowChatMessage(
            mockMessage, mockSender, mockChatParams
        )

        assertNotNull(testSubscriber.chatEvent)
        assertEquals("Hello Server!", testSubscriber.chatEvent?.message)
        assertFalse(isVanillaBroadcastAllowed)
    }

    @Test
    @Disabled("Requires a full Fabric Server environment due to intermediary field obfuscation (field_xxx) blocking Mockito.")
    fun `test cancelled lux chat event blocks delivery`() {
        val mockServer = mock<MinecraftServer>()
        val mockPlayerList = mock<PlayerList>()
        val mockSender = mock<ServerPlayer>()
        val mockMessage = mock<PlayerChatMessage>()
        val mockChatParams = mock<ChatType.Bound>()

        // This fails in unit tests because 'server' is obfuscated to 'field_xxx' at runtime
        whenever(mockSender.server).thenReturn(mockServer)
        whenever(mockServer.playerList).thenReturn(mockPlayerList)
        whenever(mockMessage.signedContent()).thenReturn("Bad Words")

        val cancelSubscriber = object {
            @Subscribe
            fun onChat(event: PlayerChatEvent) {
                event.isCancelled = true
            }
        }
        EventBus.register(cancelSubscriber)

        val isVanillaBroadcastAllowed = ServerMessageEvents.ALLOW_CHAT_MESSAGE.invoker().allowChatMessage(
            mockMessage, mockSender, mockChatParams
        )

        EventBus.unregister(cancelSubscriber)

        assertFalse(isVanillaBroadcastAllowed)
    }
}