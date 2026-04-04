package com.novaco.luxapi.commons.command.injector.impl

import com.novaco.luxapi.commons.command.exception.CommandParseException
import com.novaco.luxapi.commons.math.Vector3D
import com.novaco.luxapi.commons.player.LuxPlayer
import com.novaco.luxapi.commons.player.PlayerManager
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.UUID

/**
 * A dummy player strictly for testing injector lookups by matching specific names.
 */
class DummyCommandPlayer(override val uniqueId: UUID, override val name: String) : LuxPlayer {
    override val parent: Any = Any()
    override val locale: String = "en_us"
    override val position: Vector3D = Vector3D(0.0, 0.0, 0.0)

    override fun sendMessage(message: String) {}
    override fun hasPermission(permission: String): Boolean = true
    override fun isPlayer(): Boolean = true
    override fun sendTitle(title: String, subtitle: String) {}
    override fun kick(reason: String) {}
}

/**
 * A simple mock of the PlayerManager to simulate player lookup.
 */
class MockPlayerManager : PlayerManager {
    private val players = mutableMapOf<String, LuxPlayer>()

    fun addPlayer(player: LuxPlayer) {
        players[player.name.lowercase()] = player
    }

    override fun getPlayer(name: String): LuxPlayer? = players[name.lowercase()]
    override fun getPlayer(uuid: UUID): LuxPlayer? = players.values.find { it.uniqueId == uuid }
    override fun getOnlinePlayers(): List<LuxPlayer> = players.values.toList()
}

class PlayerInjectorTest {

    private lateinit var playerManager: MockPlayerManager
    private lateinit var injector: PlayerInjector
    private lateinit var dummySender: DummyCommandSender

    @BeforeEach
    fun setup() {
        playerManager = MockPlayerManager()
        injector = PlayerInjector(playerManager)
        dummySender = DummyCommandSender()
    }

    @Test
    fun `test successful player injection`() {
        val targetUuid = UUID.randomUUID()
        val targetPlayer = DummyCommandPlayer(targetUuid, "NovacoAdmin")
        playerManager.addPlayer(targetPlayer)

        val args = arrayOf("NovacoAdmin")
        val injected = injector.instantiate(dummySender, args, 0)

        assertNotNull(injected)
        assertEquals("NovacoAdmin", injected?.name)
        assertEquals(targetUuid, injected?.uniqueId)
    }

    @Test
    fun `test injection fails when player is offline`() {
        val args = arrayOf("OfflineUser")

        val exception = assertThrows(CommandParseException::class.java) {
            injector.instantiate(dummySender, args, 0)
        }

        assertTrue(exception.message!!.contains("Could not find player"), "Should throw specific error for offline players.")
    }

    @Test
    fun `test injection fails when argument is missing`() {
        val args = emptyArray<String>()

        assertThrows(CommandParseException::class.java) {
            injector.instantiate(dummySender, args, 0)
        }
    }
}