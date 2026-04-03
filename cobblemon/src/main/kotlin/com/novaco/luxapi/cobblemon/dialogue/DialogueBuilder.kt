package com.novaco.luxapi.cobblemon.dialogue

import com.cobblemon.mod.common.api.dialogue.Dialogue
import com.cobblemon.mod.common.api.dialogue.DialoguePage
import com.cobblemon.mod.common.api.dialogue.FunctionDialogueAction
import com.cobblemon.mod.common.api.dialogue.WrappedDialogueText
import com.cobblemon.mod.common.api.dialogue.input.DialogueOption
import com.cobblemon.mod.common.api.dialogue.input.DialogueOptionSetInput
import com.cobblemon.mod.common.util.closeDialogue
import com.cobblemon.mod.common.util.openDialogue
import com.novaco.luxapi.commons.player.LuxPlayer
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer

/**
 * A fluent builder for creating complex, branching dialogues effortlessly.
 */
class DialogueBuilder(private val defaultSpeaker: String) {

    private val nodes = mutableMapOf<String, StoryNode>()
    private var startNodeId: String? = null

    /**
     * Creates a new message node in the dialogue tree.
     */
    fun createNode(
        id: String,
        text: String,
        speaker: String = defaultSpeaker,
        isStart: Boolean = false
    ): StoryNode {
        val node = StoryNode(id, speaker, text)
        nodes[id] = node

        if (isStart || startNodeId == null) {
            startNodeId = id
        }
        return node
    }

    /**
     * Opens the constructed dialogue tree for a specific player.
     * Integrates with the native Cobblemon DialogueManager.
     */
    fun openFor(player: LuxPlayer) {
        val serverPlayer = player.parent as ServerPlayer
        val startId = startNodeId ?: return

        val initialDialogue = convertToNative(startId, serverPlayer) ?: return
        serverPlayer.openDialogue(initialDialogue)
    }

    /**
     * Converts our StoryNode into Cobblemon's native Dialogue components.
     * Uses recursive generation and chaining openDialogue to handle branching.
     */
    private fun convertToNative(nodeId: String, player: ServerPlayer): Dialogue? {
        val node = nodes[nodeId] ?: return null
        val options = mutableListOf<DialogueOption>()

        if (node.choices.isNotEmpty()) {
            for (choice in node.choices) {
                options.add(
                    DialogueOption(
                        text = WrappedDialogueText(Component.literal(choice.text)),
                        action = FunctionDialogueAction { _, _ ->
                            choice.action?.invoke()

                            val nextDialogue = convertToNative(choice.targetNodeId, player)
                            if (nextDialogue != null) {
                                player.openDialogue(nextDialogue)
                            } else {
                                player.closeDialogue()
                            }
                        }
                    )
                )
            }
        } else if (node.nextNodeId != null) {
            options.add(
                DialogueOption(
                    text = WrappedDialogueText(Component.literal("Continue...")),
                    action = FunctionDialogueAction { _, _ ->
                        val nextDialogue = convertToNative(node.nextNodeId!!, player)
                        if (nextDialogue != null) {
                            player.openDialogue(nextDialogue)
                        } else {
                            player.closeDialogue()
                        }
                    }
                )
            )
        } else {
            options.add(
                DialogueOption(
                    text = WrappedDialogueText(Component.literal("Close")),
                    action = FunctionDialogueAction { _, _ ->
                        player.closeDialogue()
                    }
                )
            )
        }

        val input = DialogueOptionSetInput(options)

        val page = DialoguePage.of(
            id = node.id,
            speaker = node.speakerName,
            lines = listOf(Component.literal(node.text)),
            input = input
        )

        return Dialogue(
            listOf(page),
            net.minecraft.resources.ResourceLocation.parse("luxapi:${node.id.lowercase().replace(" ", "_")}")
        )
    }
}