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
 * A sub-builder for configuring a DialoguePage with multiple choices.
 */
class LuxChoicePageBuilder(
    private val id: String,
    private val speakerId: String?,
    private val text: String
) {
    private val options = mutableListOf<DialogueOption>()
    var verticalLayout: Boolean = false

    /**
     * Adds a clickable option to the dialogue page.
     * @param text The text displayed on the button.
     * @param targetPageId The ID of the page to jump to. If null, the dialogue closes.
     * @param action An optional action block to execute when the button is clicked.
     */
    @JvmOverloads
    fun option(
        text: String,
        targetPageId: String? = null,
        action: ((ServerPlayer, ActiveDialogue) -> Unit)? = null
    ): LuxChoicePageBuilder {

        // Value must be unique so Cobblemon can track the packet correctly.
        val optionValue = text.lowercase().replace(" ", "_")

        val dialogueOption = DialogueOption(
            text = WrappedDialogueText(text.text()),
            value = optionValue,
            action = FunctionDialogueAction { activeDialogue, _ ->
                action?.invoke(activeDialogue.playerEntity, activeDialogue)

                val nextPage = activeDialogue.dialogueReference.pages.find { it.id == targetPageId }
                if (nextPage != null) {
                    activeDialogue.setPage(nextPage)
                } else {
                    activeDialogue.close()
                }
            }
        )
        options.add(dialogueOption)
        return this
    }

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