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
 * A fluent builder for constructing complex, multi-page Cobblemon dialogues.
 * This builder simplifies the creation of branching conversations by grouping all pages
 * into a single [Dialogue] object, allowing for seamless page transitions without UI flicker.
 */
class LuxDialogueBuilder {

    private val pages = mutableListOf<DialoguePage>()
    private val speakers = mutableMapOf<String, DialogueSpeaker>()
    private var defaultBackground: ResourceLocation = Dialogue.DEFAULT_BACKGROUND

    /**
     * Registers a speaker who can be referenced in dialogue pages.
     *
     * @param id A unique identifier for this speaker.
     * @param name The name to be displayed in the dialogue box.
     * @param facePath An optional resource path to a texture for the speaker's portrait (e.g., "cobblemon:textures/gui/dialogue/oak.png").
     * @return This [LuxDialogueBuilder] instance for method chaining.
     */
    fun addSpeaker(id: String, name: String, facePath: String? = null): LuxDialogueBuilder {
        val speaker = DialogueSpeaker().of(name.text())
        // Note: The 'facePath' parameter is reserved for future implementation of custom portraits.
        speakers[id] = speaker
        return this
    }

    /**
     * Adds a standard dialogue page where the player clicks to advance.
     *
     * @param id A unique identifier for this page.
     * @param speakerId The ID of the speaker for this page, if any.
     * @param text The text content of the page.
     * @param nextPageId The ID of the page to navigate to next. If null, the dialogue closes.
     * @param onEnter An optional action to execute when the player enters this page.
     * @return This [LuxDialogueBuilder] instance for method chaining.
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
     * Adds a page that automatically advances to the next one after a short delay.
     *
     * @param id A unique identifier for this page.
     * @param speakerId The ID of the speaker for this page, if any.
     * @param text The text content of the page.
     * @param nextPageId The ID of the page to navigate to next. Note: Due to Cobblemon limitations, this relies on page order.
     * @param showTimer Whether to display a visual timer for the auto-advance.
     * @return This [LuxDialogueBuilder] instance for method chaining.
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
            // Note: Cobblemon's DialogueAutoContinueInput hardcodes the action to simply increment the page index.
            // For custom routing, ensure pages are added in the desired sequence.
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
     * Adds a page that presents the player with multiple choices.
     *
     * @param id A unique identifier for this page.
     * @param speakerId The ID of the speaker for this page, if any.
     * @param text The question or statement to present before the choices.
     * @param setup A lambda function to configure the choices using the [LuxChoicePageBuilder].
     * @return This [LuxDialogueBuilder] instance for method chaining.
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
     * Sets a custom background texture for the entire dialogue.
     *
     * @param texturePath The resource path to the background texture.
     * @return This [LuxDialogueBuilder] instance for method chaining.
     */
    fun setBackground(texturePath: String): LuxDialogueBuilder {
        this.defaultBackground = ResourceLocation.parse(texturePath)
        return this
    }

    /**
     * Finalizes the construction of the dialogue and immediately displays it to the player.
     *
     * @param player The [LuxPlayer] who will see the dialogue.
     * @param npc An optional [NPCEntity] to associate with the dialogue, making them the primary speaker.
     * @return The [ActiveDialogue] instance that was created.
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