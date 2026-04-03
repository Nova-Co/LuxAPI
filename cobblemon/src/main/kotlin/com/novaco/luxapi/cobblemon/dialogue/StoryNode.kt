package com.novaco.luxapi.cobblemon.dialogue

/**
 * Represents a single node in a dialogue tree.
 * Designed to be easily serialized/deserialized for visual story editors.
 */
data class StoryNode(
    val id: String,
    val speakerName: String,
    val text: String,
    val iconId: String? = null
) {
    val choices = mutableListOf<StoryChoice>()
    var nextNodeId: String? = null

    fun addChoice(text: String, targetNodeId: String): StoryNode {
        choices.add(StoryChoice(text, targetNodeId))
        return this
    }
}

data class StoryChoice(
    val text: String,
    val targetNodeId: String,
    val action: (() -> Unit)? = null
)