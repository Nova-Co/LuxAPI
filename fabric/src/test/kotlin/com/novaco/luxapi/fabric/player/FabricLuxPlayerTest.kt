package com.novaco.luxapi.fabric.player

import net.minecraft.SharedConstants
import net.minecraft.network.chat.Component
import net.minecraft.server.Bootstrap
import net.minecraft.server.level.ClientInformation
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.network.ServerGamePacketListenerImpl
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.mockito.ArgumentCaptor
import org.mockito.kotlin.*
import java.util.UUID

/**
 * Unit tests validating the functional wrapping and execution mapping of the FabricLuxPlayer.
 */
class FabricLuxPlayerTest {

    companion object {
        /**
         * Initializes Minecraft's internal registries.
         */
        @JvmStatic
        @BeforeAll
        fun initMinecraft() {
            SharedConstants.tryDetectVersion()
            Bootstrap.bootStrap()
        }
    }

    /**
     * Tests that all generalized properties accurately reflect the encapsulated native object.
     */
    @Test
    fun `test player properties accurately map to native server player`() {
        val mockPlayer = mock<ServerPlayer>()
        val mockClientInfo = mock<ClientInformation>()
        val playerUuid = UUID.randomUUID()

        whenever(mockPlayer.scoreboardName).thenReturn("FabricTester")
        whenever(mockPlayer.uuid).thenReturn(playerUuid)
        whenever(mockPlayer.x).thenReturn(150.5)
        whenever(mockPlayer.y).thenReturn(64.0)
        whenever(mockPlayer.z).thenReturn(-300.2)

        whenever(mockClientInfo.language()).thenReturn("en_gb")
        whenever(mockPlayer.clientInformation()).thenReturn(mockClientInfo)

        val luxPlayer = FabricLuxPlayer(mockPlayer)

        assertEquals("FabricTester", luxPlayer.name)
        assertEquals(playerUuid, luxPlayer.uniqueId)
        assertEquals(mockPlayer, luxPlayer.parent)
        assertEquals("en_gb", luxPlayer.locale)
        assertEquals(150.5, luxPlayer.position.x)
        assertEquals(64.0, luxPlayer.position.y)
        assertEquals(-300.2, luxPlayer.position.z)
        assertTrue(luxPlayer.hasPermission("any.node"))
    }

    /**
     * Verifies that string messages are correctly wrapped into native literal components.
     */
    @Test
    fun `test send message translates to native component message`() {
        val mockPlayer = mock<ServerPlayer>()
        val luxPlayer = FabricLuxPlayer(mockPlayer)

        luxPlayer.sendMessage("Hello Fabric!")

        val componentCaptor = ArgumentCaptor.forClass(Component::class.java)
        verify(mockPlayer).sendSystemMessage(componentCaptor.capture())

        val capturedComponent = componentCaptor.value
        assertEquals("Hello Fabric!", capturedComponent.string)
    }

    /**
     * Ensures the cross-platform kick method accesses and utilizes the native connection disconnect feature safely.
     */
    @Test
    fun `test kick method safely disconnects via connection field`() {
        val mockPlayer = mock<ServerPlayer>()
        val mockConnection = mock<ServerGamePacketListenerImpl>()

        val connectionField = ServerPlayer::class.java.getField("connection")
        connectionField.isAccessible = true
        connectionField.set(mockPlayer, mockConnection)

        val luxPlayer = FabricLuxPlayer(mockPlayer)
        luxPlayer.kick("Banned by Admin")

        val componentCaptor = ArgumentCaptor.forClass(Component::class.java)
        verify(mockConnection).disconnect(componentCaptor.capture())

        assertEquals("Banned by Admin", componentCaptor.value.string)
    }
}