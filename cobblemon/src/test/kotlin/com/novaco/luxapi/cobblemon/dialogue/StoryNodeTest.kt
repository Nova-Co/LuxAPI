package com.novaco.luxapi.cobblemon.dialogue

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class StoryNodeTest {

    @Test
    fun `test node creation and properties`() {
        val node = StoryNode("intro_node", "Professor", "Welcome to the world of Pokemon!")

        assertEquals("intro_node", node.id)
        assertEquals("Professor", node.speakerName)
        assertEquals("Welcome to the world of Pokemon!", node.text)
        assertNull(node.iconId)
        assertTrue(node.choices.isEmpty())
        assertNull(node.nextNodeId)
    }

    @Test
    fun `test add choice chaining`() {
        val node = StoryNode("choice_node", "NPC", "Which path will you take?")

        val result = node.addChoice("Left Path", "node_left")
            .addChoice("Right Path", "node_right")

        assertEquals(node, result, "addChoice should return the node instance for chaining.")
        assertEquals(2, node.choices.size)

        assertEquals("Left Path", node.choices[0].text)
        assertEquals("node_left", node.choices[0].targetNodeId)

        assertEquals("Right Path", node.choices[1].text)
        assertEquals("node_right", node.choices[1].targetNodeId)
    }

    @Test
    fun `test next node state transition`() {
        val node = StoryNode("seq_1", "NPC", "Let me tell you a story...")
        node.nextNodeId = "seq_2"

        assertEquals("seq_2", node.nextNodeId)
    }
}