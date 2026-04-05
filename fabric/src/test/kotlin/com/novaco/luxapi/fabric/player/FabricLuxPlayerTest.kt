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

class FabricLuxPlayerTest {

    companion object {
        @JvmStatic
        @BeforeAll
        fun initMinecraft() {
            SharedConstants.tryDetectVersion()
            Bootstrap.bootStrap()
        }
    }

    @Test
    fun `test player properties accurately map to native server player`() {
        // 1. Mock the native objects
        val mockPlayer = mock<ServerPlayer>()
        val mockClientInfo = mock<ClientInformation>()
        val playerUuid = UUID.randomUUID()

        // 2. Define their behavior using idiomatic mockito-kotlin
        whenever(mockPlayer.scoreboardName).thenReturn("FabricTester")
        whenever(mockPlayer.uuid).thenReturn(playerUuid)
        whenever(mockPlayer.x).thenReturn(150.5)
        whenever(mockPlayer.y).thenReturn(64.0)
        whenever(mockPlayer.z).thenReturn(-300.2)

        whenever(mockClientInfo.language()).thenReturn("en_gb")
        whenever(mockPlayer.clientInformation()).thenReturn(mockClientInfo)

        // 3. Wrap it
        val luxPlayer = FabricLuxPlayer(mockPlayer)

        // 4. Verify
        assertEquals("FabricTester", luxPlayer.name)
        assertEquals(playerUuid, luxPlayer.uniqueId)
        assertEquals(mockPlayer, luxPlayer.parent, "The parent must expose the raw ServerPlayer instance.")
        assertEquals("en_gb", luxPlayer.locale)
        assertEquals(150.5, luxPlayer.position.x)
        assertEquals(64.0, luxPlayer.position.y)
        assertEquals(-300.2, luxPlayer.position.z)
        assertTrue(luxPlayer.hasPermission("any.node"), "Fabric implementation currently defaults to true.")
    }

    @Test
    fun `test send message translates to native component message`() {
        val mockPlayer = mock<ServerPlayer>()
        val luxPlayer = FabricLuxPlayer(mockPlayer)

        luxPlayer.sendMessage("Hello Fabric!")

        // Capture the component passed to the native send method
        val componentCaptor = ArgumentCaptor.forClass(Component::class.java)
        verify(mockPlayer).sendSystemMessage(componentCaptor.capture())

        val capturedComponent = componentCaptor.value
        assertEquals("Hello Fabric!", capturedComponent.string, "The message should be wrapped in a Component literal.")
    }

    @Test
    fun `test kick method safely disconnects via connection field`() {
        val mockPlayer = mock<ServerPlayer>()
        val mockConnection = mock<ServerGamePacketListenerImpl>()

        // Because 'connection' is a public field (not a method) in Mojang mappings,
        // we use Reflection to inject our mock so we don't get a NullPointerException!
        val connectionField = ServerPlayer::class.java.getField("connection")
        connectionField.isAccessible = true
        connectionField.set(mockPlayer, mockConnection)

        val luxPlayer = FabricLuxPlayer(mockPlayer)
        luxPlayer.kick("Banned by Admin")

        // Capture the component passed to the disconnect method
        val componentCaptor = ArgumentCaptor.forClass(Component::class.java)
        verify(mockConnection).disconnect(componentCaptor.capture())

        assertEquals("Banned by Admin", componentCaptor.value.string)
    }
}