package com.novaco.luxapi.cobblemon.dialogue

import com.cobblemon.mod.common.api.dialogue.ActiveDialogue
import com.cobblemon.mod.common.api.dialogue.Dialogue
import com.cobblemon.mod.common.api.dialogue.DialogueManager
import com.cobblemon.mod.common.api.dialogue.DialoguePage
import com.cobblemon.mod.common.api.dialogue.DialogueSpeaker
import com.cobblemon.mod.common.api.dialogue.FunctionDialogueAction
import com.cobblemon.mod.common.api.dialogue.input.DialogueAutoContinueInput
import com.cobblemon.mod.common.api.dialogue.input.DialogueNoInput
import com.cobblemon.mod.common.api.text.text
import com.cobblemon.mod.common.entity.npc.NPCEntity
import com.novaco.luxapi.commons.player.LuxPlayer
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer

/**
 * A highly fluent builder for creating Cobblemon dialogues.
 * Instead of re-opening dialogues for branching, this builder groups all pages
 * into a single [Dialogue] object and uses internal page jumping for a flicker-free UI.
 */
class LuxDialogueBuilder {

    private val pages = mutableListOf<DialoguePage>()
    private val speakers = mutableMapOf<String, DialogueSpeaker>()
    private var defaultBackground: ResourceLocation = Dialogue.DEFAULT_BACKGROUND

    /**
     * Registers a speaker with an optional custom portrait face.
     * @param id The ID used to reference this speaker in pages.
     * @param name The display name in the dialogue box.
     * @param facePath Example: "cobblemon:textures/gui/dialogue/oak.png"
     */
    fun addSpeaker(id: String, name: String, facePath: String? = null): LuxDialogueBuilder {
        val speaker = DialogueSpeaker().of(name.text())
        // (Optional) If you want to use the DialogueFaceProvider in the future,
        // you would register the resource location to the 'face' argument.
        speakers[id] = speaker
        return this
    }

    /**
     * Adds a simple linear page where the player clicks anywhere to continue.
     */
    @JvmOverloads
    fun addPage(
        id: String,
        speakerId: String? = null,
        text: String,
        nextPageId: String? = null,
        onEnter: ((ActiveDialogue) -> Unit)? = null
    ): LuxDialogueBuilder {
        val input = DialogueNoInput().apply {
            action = FunctionDialogueAction { dialogue, _ ->
                onEnter?.invoke(dialogue)
                val nextPage = dialogue.dialogueReference.pages.find { it.id == nextPageId }
                if (nextPage != null) {
                    dialogue.setPage(nextPage)
                } else {
                    dialogue.close()
                }
            }
        }

        val page = DialoguePage.of(
            id = id,
            speaker = speakerId,
            lines = listOf(text.text()),
            input = input
        )
        pages.add(page)
        return this
    }

    /**
     * Adds a page that automatically transitions to the next page after a delay.
     */
    fun addAutoPage(
        id: String,
        speakerId: String? = null,
        text: String,
        nextPageId: String? = null,
        showTimer: Boolean = false
    ): LuxDialogueBuilder {
        val input = DialogueAutoContinueInput().apply {
            this.showTimer = showTimer
            // Unfortunately, Cobblemon's DialogueAutoContinueInput hardcodes action to incrementPage.
            // For custom routing, we rely on standard next-page index logic for auto pages.
        }

        val page = DialoguePage.of(
            id = id,
            speaker = speakerId,
            lines = listOf(text.text()),
            input = input
        )
        pages.add(page)
        return this
    }

    /**
     * Initiates the building of a choice-based page (Branching).
     */
    fun addChoicePage(
        id: String,
        speakerId: String? = null,
        text: String,
        setup: LuxChoicePageBuilder.() -> Unit
    ): LuxDialogueBuilder {
        val choiceBuilder = LuxChoicePageBuilder(id, speakerId, text)
        choiceBuilder.setup()
        pages.add(choiceBuilder.build())
        return this
    }

    /**
     * Changes the default dialogue background.
     */
    fun setBackground(texturePath: String): LuxDialogueBuilder {
        this.defaultBackground = ResourceLocation.parse(texturePath)
        return this
    }

    /**
     * Builds and immediately opens the dialogue for the specified player.
     * @param npc Optional NPC entity to bind to the dialogue.
     */
    @JvmOverloads
    fun buildAndOpen(player: LuxPlayer, npc: NPCEntity? = null): ActiveDialogue {
        val serverPlayer = player.parent as ServerPlayer

        val dialogue = Dialogue.of(
            pages = pages,
            background = defaultBackground,
            escapeAction = { activeDialogue -> activeDialogue.close() },
            speakers = speakers
        )

        return if (npc != null) {
            DialogueManager.startDialogue(serverPlayer, npc, dialogue)
        } else {
            DialogueManager.startDialogue(serverPlayer, dialogue)
        }
    }
}