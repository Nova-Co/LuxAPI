package com.novaco.luxapi.cobblemon.dialogue

import com.novaco.luxapi.commons.player.LuxPlayer
import net.minecraft.server.level.ServerPlayer
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@Disabled("Requires Minecraft server level registry and Cobblemon Engine loaded.")
class DialogueBuilderTest {

    @Test
    fun `test node creation and automatic start node assignment`() {
        val builder = DialogueBuilder("Guide")

        // The first created node should default to the start node
        val introNode = builder.createNode("intro", "Hello!")
        assertEquals("intro", introNode.id)
        assertEquals("Guide", introNode.speakerName, "Should use default speaker if not provided.")

        // Subsequent nodes should not override the start node unless explicitly stated
        val stepNode = builder.createNode("step_2", "Follow me.", speaker = "Guard")
        assertEquals("Guard", stepNode.speakerName)

        // Explicitly overriding the start node
        val overrideNode = builder.createNode("alt_intro", "Wait, start here!", isStart = true)

        // Verify internal state using Reflection
        val startNodeField = DialogueBuilder::class.java.getDeclaredField("startNodeId")
        startNodeField.isAccessible = true
        val startNodeId = startNodeField.get(builder) as String

        assertEquals("alt_intro", startNodeId)
    }

    @Test
    fun `test openFor exits safely if no start node exists`() {
        val builder = DialogueBuilder("Guide")
        val mockLuxPlayer = mock<LuxPlayer>()
        val mockServerPlayer = mock<ServerPlayer>()

        whenever(mockLuxPlayer.parent).thenReturn(mockServerPlayer)

        assertDoesNotThrow("openFor should return early without crashing if startNodeId is null") {
            builder.openFor(mockLuxPlayer)
        }
    }

    @Test
    fun `test graph construction and internal node mapping`() {
        val builder = DialogueBuilder("Narrator")
        builder.createNode("start", "Path split")
            .addChoice("Go A", "node_a")
            .addChoice("Go B", "node_b")

        builder.createNode("node_a", "You chose A")
        builder.createNode("node_b", "You chose B")

        val nodesField = DialogueBuilder::class.java.getDeclaredField("nodes")
        nodesField.isAccessible = true

        @Suppress("UNCHECKED_CAST")
        val internalNodes = nodesField.get(builder) as Map<String, StoryNode>

        assertEquals(3, internalNodes.size)
        assertTrue(internalNodes.containsKey("start"))
        assertTrue(internalNodes.containsKey("node_a"))
        assertTrue(internalNodes.containsKey("node_b"))
        assertEquals(2, internalNodes["start"]?.choices?.size)
    }
}