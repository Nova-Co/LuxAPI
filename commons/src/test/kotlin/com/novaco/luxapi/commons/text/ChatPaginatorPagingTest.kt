package com.novaco.luxapi.commons.text

import com.novaco.luxapi.commons.command.sender.CommandSender
import com.novaco.luxapi.commons.math.Vector3D
import com.novaco.luxapi.commons.player.LuxPlayer
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.UUID

/**
 * A dummy player that records every message sent to it, for asserting on [ChatPaginator]'s
 * real output rather than a re-implementation of its pagination math.
 */
class RecordingCommandPlayer : LuxPlayer {
    override val uniqueId: UUID = UUID.randomUUID()
    override val name: String = "Recorder"
    override val parent: Any = Any()
    override val locale: String = "en_us"
    override val position: Vector3D = Vector3D(0.0, 0.0, 0.0)

    val sentMessages = mutableListOf<String>()

    override fun sendMessage(message: String) {
        sentMessages.add(message)
    }
    override fun hasPermission(permission: String): Boolean = true
    override fun sendTitle(title: String, subtitle: String) {}
    override fun kick(reason: String) {}
}

class ChatPaginatorPagingTest {

    private lateinit var paginator: ChatPaginator

    @BeforeEach
    fun setup() {
        paginator = ChatPaginator(
            items = listOf("Line 1", "Line 2", "Line 3", "Line 4", "Line 5"),
            linesPerPage = 2,
            header = "HEADER",
            commandPrefix = "/pages"
        )
    }

    @Test
    fun `test getPageLines returns header plus the item lines for that page`() {
        assertEquals(listOf("HEADER", "Line 1", "Line 2"), paginator.getPageLines(1))
        assertEquals(listOf("HEADER", "Line 5"), paginator.getPageLines(3))
    }

    @Test
    fun `test getPageLines coerces an out-of-range page into bounds`() {
        assertEquals(paginator.getPageLines(3), paginator.getPageLines(99))
        assertEquals(paginator.getPageLines(1), paginator.getPageLines(0))
    }

    @Test
    fun `test getPageLines on an empty item list returns the empty-state line`() {
        val empty = ChatPaginator(items = emptyList(), header = "HEADER", commandPrefix = "/pages")

        assertEquals(listOf("HEADER", "<gray><i>No entries found.</i></gray>"), empty.getPageLines(1))
    }

    @Test
    fun `test getFooterLine reflects the current and total page count`() {
        assertTrue(paginator.getFooterLine(2).contains("Page 2 of 3"))
    }

    @Test
    fun `test sendPage sends the same content getPageLines and getFooterLine compute`() {
        val player = RecordingCommandPlayer()

        paginator.sendPage(player, 2)

        val expected = paginator.getPageLines(2) + paginator.getFooterLine(2)
        assertEquals(expected, player.sentMessages)
    }
}
