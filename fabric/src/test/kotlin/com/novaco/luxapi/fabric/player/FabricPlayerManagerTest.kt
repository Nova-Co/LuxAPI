package com.novaco.luxapi.fabric.player

import net.minecraft.SharedConstants
import net.minecraft.server.Bootstrap
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.players.PlayerList
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*
import java.util.UUID

class FabricPlayerManagerTest {

    companion object {
        @JvmStatic
        @BeforeAll
        fun initMinecraft() {
            SharedConstants.tryDetectVersion()
            Bootstrap.bootStrap()
        }
    }

    @Test
    fun `test get player by name`() {
        val mockServer = mock<MinecraftServer>()
        val mockPlayerList = mock<PlayerList>()
        val mockPlayer = mock<ServerPlayer>()

        whenever(mockServer.playerList).thenReturn(mockPlayerList)
        whenever(mockPlayerList.getPlayerByName("ValidPlayer")).thenReturn(mockPlayer)
        whenever(mockPlayerList.getPlayerByName("OfflinePlayer")).thenReturn(null)

        val manager = FabricPlayerManager(mockServer)

        // Verify successful retrieval
        val found = manager.getPlayer("ValidPlayer")
        assertNotNull(found, "Manager should return a wrapped LuxPlayer for online players.")
        assertEquals(mockPlayer, found?.parent, "The retrieved player's parent should match the native mock.")

        // Verify failure state
        val notFound = manager.getPlayer("OfflinePlayer")
        assertNull(notFound, "Manager should return null if the player is not found.")
    }

    @Test
    fun `test get player by uuid`() {
        val mockServer = mock<MinecraftServer>()
        val mockPlayerList = mock<PlayerList>()
        val mockPlayer = mock<ServerPlayer>()
        val targetUuid = UUID.randomUUID()
        val missingUuid = UUID.randomUUID()

        whenever(mockServer.playerList).thenReturn(mockPlayerList)
        whenever(mockPlayerList.getPlayer(targetUuid)).thenReturn(mockPlayer)
        whenever(mockPlayerList.getPlayer(missingUuid)).thenReturn(null)

        val manager = FabricPlayerManager(mockServer)

        assertNotNull(manager.getPlayer(targetUuid))
        assertNull(manager.getPlayer(missingUuid))
    }

    @Test
    fun `test get online players returns mapped list`() {
        val mockServer = mock<MinecraftServer>()
        val mockPlayerList = mock<PlayerList>()

        // Create a list of 3 mock native players
        val nativePlayers = listOf(mock<ServerPlayer>(), mock<ServerPlayer>(), mock<ServerPlayer>())

        whenever(mockServer.playerList).thenReturn(mockPlayerList)
        whenever(mockPlayerList.players).thenReturn(nativePlayers)

        val manager = FabricPlayerManager(mockServer)
        val onlinePlayers = manager.getOnlinePlayers()

        assertEquals(3, onlinePlayers.size, "Should map exactly 3 native players.")

        // Verify they are correctly wrapped
        assertTrue(onlinePlayers.all { it is FabricLuxPlayer }, "All elements should be mapped to FabricLuxPlayer.")
        assertEquals(nativePlayers[0], onlinePlayers[0].parent)
    }
}