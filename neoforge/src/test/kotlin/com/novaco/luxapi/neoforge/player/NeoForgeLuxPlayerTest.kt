package com.novaco.luxapi.neoforge.player

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

class NeoForgeLuxPlayerTest {

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
        whenever(mockPlayer.scoreboardName).thenReturn("ForgeTester")
        whenever(mockPlayer.uuid).thenReturn(playerUuid)
        whenever(mockPlayer.x).thenReturn(150.5)
        whenever(mockPlayer.y).thenReturn(64.0)
        whenever(mockPlayer.z).thenReturn(-300.2)

        whenever(mockClientInfo.language()).thenReturn("en_us")
        whenever(mockPlayer.clientInformation()).thenReturn(mockClientInfo)

        // 3. Wrap it in our NeoForge implementation
        val luxPlayer = NeoForgeLuxPlayer(mockPlayer)

        // 4. Verify properties
        assertEquals("ForgeTester", luxPlayer.name)
        assertEquals(playerUuid, luxPlayer.uniqueId)
        assertEquals(mockPlayer, luxPlayer.parent, "The parent must expose the raw ServerPlayer instance.")
        assertEquals("en_us", luxPlayer.locale)
        assertEquals(150.5, luxPlayer.position.x)
        assertEquals(64.0, luxPlayer.position.y)
        assertEquals(-300.2, luxPlayer.position.z)
        assertTrue(luxPlayer.hasPermission("any.node"), "Implementation defaults to true pending NeoForge API injection.")
    }

    @Test
    fun `test send message translates to native component message`() {
        val mockPlayer = mock<ServerPlayer>()
        val luxPlayer = NeoForgeLuxPlayer(mockPlayer)

        luxPlayer.sendMessage("Hello NeoForge!")

        // Capture the component passed to the native send method
        val componentCaptor = ArgumentCaptor.forClass(Component::class.java)
        verify(mockPlayer).sendSystemMessage(componentCaptor.capture())

        val capturedComponent = componentCaptor.value
        assertEquals("Hello NeoForge!", capturedComponent.string, "The message should be wrapped in a Component literal.")
    }

    @Test
    fun `test kick method safely disconnects via connection field`() {
        val mockPlayer = mock<ServerPlayer>()
        val mockConnection = mock<ServerGamePacketListenerImpl>()

        // Use Reflection to safely inject the connection field to prevent NPEs
        val connectionField = ServerPlayer::class.java.getField("connection")
        connectionField.isAccessible = true
        connectionField.set(mockPlayer, mockConnection)

        val luxPlayer = NeoForgeLuxPlayer(mockPlayer)
        luxPlayer.kick("Banned by Forge Admin")

        // Capture the component passed to the disconnect method
        val componentCaptor = ArgumentCaptor.forClass(Component::class.java)
        verify(mockConnection).disconnect(componentCaptor.capture())

        assertEquals("Banned by Forge Admin", componentCaptor.value.string)
    }
}