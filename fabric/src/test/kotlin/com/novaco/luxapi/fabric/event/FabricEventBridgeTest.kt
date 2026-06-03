package com.novaco.luxapi.fabric.event

import com.novaco.luxapi.commons.event.EventBus
import com.novaco.luxapi.commons.event.Subscribe
import com.novaco.luxapi.commons.event.player.PlayerChatEvent
import com.novaco.luxapi.commons.event.player.PlayerJoinEvent
import com.novaco.luxapi.commons.event.player.PlayerQuitEvent
import com.novaco.luxapi.fabric.LuxFabricInitializer
import com.novaco.luxapi.fabric.player.FabricLuxPlayer
import com.novaco.luxapi.fabric.player.FabricPlayerManager
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
 * Unit tests evaluating the translation of native Fabric events into cross-platform LuxAPI events.
 */
class FabricEventBridgeTest {

    private lateinit var testSubscriber: TestBridgeSubscriber

    companion object {
        /**
         * Initializes the server registries and registers the bridge globally.
         */
        @JvmStatic
        @BeforeAll
        fun initBridge() {
            SharedConstants.tryDetectVersion()
            Bootstrap.bootStrap()
            FabricEventBridge.register()
        }
    }

    /**
     * Sets up the event subscribers and injects a mock PlayerManager.
     */
    @BeforeEach
    fun setup() {
        testSubscriber = TestBridgeSubscriber()
        EventBus.register(testSubscriber)

        val mockManager = mock<FabricPlayerManager>()
        val field = LuxFabricInitializer::class.java.getDeclaredField("playerManager")
        field.isAccessible = true
        field.set(null, mockManager)

        whenever(mockManager.login(any())).thenAnswer { FabricLuxPlayer(it.getArgument(0)) }
        whenever(mockManager.getPlayer(any<UUID>())).thenAnswer {
            val p = mock<ServerPlayer>()
            whenever(p.uuid).thenReturn(it.getArgument(0))
            FabricLuxPlayer(p)
        }
    }

    /**
     * Unregisters the subscriber to ensure a clean slate for the next test.
     */
    @AfterEach
    fun teardown() {
        EventBus.unregister(testSubscriber)
    }

    /**
     * Verifies that the native JOIN event successfully delegates to the cross-platform PlayerJoinEvent.
     */
    @Test
    fun `test fabric join event translates to lux join event`() {
        val mockHandler = mock<ServerGamePacketListenerImpl>()
        val mockPlayer = mock<ServerPlayer>()
        val mockSender = mock<PacketSender>()
        val mockServer = mock<MinecraftServer>()

        val playerField = ServerGamePacketListenerImpl::class.java.getField("player")
        playerField.isAccessible = true
        playerField.set(mockHandler, mockPlayer)

        ServerPlayConnectionEvents.JOIN.invoker().onPlayReady(mockHandler, mockSender, mockServer)

        assertTrue(testSubscriber.joinFired)
    }

    /**
     * Verifies that the native DISCONNECT event successfully delegates to the cross-platform PlayerQuitEvent.
     */
    @Test
    fun `test fabric disconnect event translates to lux quit event`() {
        val mockHandler = mock<ServerGamePacketListenerImpl>()
        val mockPlayer = mock<ServerPlayer>()
        val mockServer = mock<MinecraftServer>()

        whenever(mockPlayer.uuid).thenReturn(UUID.randomUUID())

        val playerField = ServerGamePacketListenerImpl::class.java.getField("player")
        playerField.isAccessible = true
        playerField.set(mockHandler, mockPlayer)

        ServerPlayConnectionEvents.DISCONNECT.invoker().onPlayDisconnect(mockHandler, mockServer)

        assertTrue(testSubscriber.quitFired)
    }

    /**
     * Validates that standard chat messages are caught, formatted, and delivered correctly.
     */
    @Test
    @Disabled("Requires a full Fabric Server environment due to intermediary field obfuscation blocking Mockito.")
    fun `test fabric chat event translates formats and broadcasts correctly`() {
        val mockServer = mock<MinecraftServer>()
        val mockPlayerList = mock<PlayerList>()
        val mockSender = mock<ServerPlayer>()
        val mockMessage = mock<PlayerChatMessage>()
        val mockChatParams = mock<ChatType.Bound>()

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

    /**
     * Ensures that cancelling the cross-platform chat event halts native delivery.
     */
    @Test
    @Disabled("Requires a full Fabric Server environment due to intermediary field obfuscation blocking Mockito.")
    fun `test cancelled lux chat event blocks delivery`() {
        val mockServer = mock<MinecraftServer>()
        val mockPlayerList = mock<PlayerList>()
        val mockSender = mock<ServerPlayer>()
        val mockMessage = mock<PlayerChatMessage>()
        val mockChatParams = mock<ChatType.Bound>()

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