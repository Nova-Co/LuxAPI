package com.novaco.luxapi.cobblemon.dialogue

import com.cobblemon.mod.common.api.dialogue.DialoguePage
import com.cobblemon.mod.common.api.dialogue.FunctionDialogueAction
import com.cobblemon.mod.common.api.dialogue.WrappedDialogueText
import com.cobblemon.mod.common.api.dialogue.input.DialogueOption
import com.cobblemon.mod.common.api.dialogue.input.DialogueOptionSetInput
import com.cobblemon.mod.common.api.text.text
import net.minecraft.server.level.ServerPlayer
import com.cobblemon.mod.common.api.dialogue.ActiveDialogue

/**
 * A fluent builder specifically for creating a [DialoguePage] that presents multiple choices to the player.
 * This is a component of the main [LuxDialogueBuilder].
 *
 * @param id The unique identifier for this dialogue page.
 * @param speakerId The optional identifier for the speaker of this page.
 * @param text The main text content to be displayed above the choices.
 */
class LuxChoicePageBuilder(
    private val id: String,
    private val speakerId: String?,
    private val text: String
) {
    private val options = mutableListOf<DialogueOption>()
    var verticalLayout: Boolean = false

    /**
     * Adds a clickable choice (an option button) to the dialogue page.
     *
     * @param text The text to be displayed on the button.
     * @param targetPageId The ID of the dialogue page to navigate to when this option is clicked. If null, the dialogue will close.
     * @param action An optional lambda function to be executed on the server when the player clicks this option.
     * @return This [LuxChoicePageBuilder] instance for method chaining.
     */
    @JvmOverloads
    fun option(
        text: String,
        targetPageId: String? = null,
        action: ((ServerPlayer, ActiveDialogue) -> Unit)? = null
    ): LuxChoicePageBuilder {

        val optionValue = text.lowercase().replace(" ", "_")

        val dialogueOption = DialogueOption(
            text = WrappedDialogueText(text.text()),
            value = optionValue,
            action = FunctionDialogueAction { activeDialogue, _ ->
                val nextPage = activeDialogue.dialogueReference.pages.find { it.id == targetPageId }

                if (nextPage != null) {
                    activeDialogue.setPage(nextPage)
                    action?.invoke(activeDialogue.playerEntity, activeDialogue)
                } else {
                    activeDialogue.close()
                    action?.invoke(activeDialogue.playerEntity, activeDialogue)
                }
            }
        )
        options.add(dialogueOption)
        return this
    }

    /**
     * Internal function to construct the final [DialoguePage] from the configured options.
     * This is called by the parent [LuxDialogueBuilder].
     *
     * @return The fully constructed [DialoguePage] with all its choices.
     */
    internal fun build(): DialoguePage {
        val input = DialogueOptionSetInput(options, vertical = verticalLayout)
        return DialoguePage.of(
            id = id,
            speaker = speakerId,
            lines = listOf(text.text()),
            input = input
        )
    }
}