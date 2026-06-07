package com.novaco.luxapi.cobblemon.dialogue

import com.cobblemon.mod.common.api.dialogue.DialoguePage
import com.cobblemon.mod.common.api.dialogue.FunctionDialogueAction
import com.cobblemon.mod.common.api.dialogue.WrappedDialogueText
import com.cobblemon.mod.common.api.dialogue.input.DialogueOption
import com.cobblemon.mod.common.api.dialogue.input.DialogueOptionSetInput
import com.cobblemon.mod.common.api.text.text
import net.minecraft.server.level.ServerPlayer
import com.cobblemon.mod.common.api.dialogue.ActiveDialogue
import com.cobblemon.mod.common.api.dialogue.DialogueGibber
import com.cobblemon.mod.common.api.dialogue.FunctionDialoguePredicate
import net.minecraft.resources.ResourceLocation

/**
 * A fluent builder specifically for creating a [DialoguePage] that presents multiple choices to the player.
 * It integrates natively with Cobblemon's options, predicates (visibility/selectability), and UI elements.
 *
 * @param id The unique identifier for this dialogue page.
 * @param speakerId The optional identifier for the speaker of this page.
 * @param text The main text content to be displayed above the choices.
 */
class ChoicePageBuilder(
    private val id: String,
    private val speakerId: String?,
    private val text: String
) {
    private val options = mutableListOf<DialogueOption>()

    /** Whether the choice buttons should be stacked vertically. */
    var verticalLayout: Boolean = false

    /** Optional hex color code or basic color string for the main text. */
    var textColor: String? = null

    /** Optional specific sound effect properties for this page. */
    var gibber: DialogueGibber? = null

    /** Optional specific background texture for this page. */
    var background: ResourceLocation? = null

    /**
     * Adds a clickable choice (an option button) to the dialogue page with full predicate support.
     *
     * @param text The text to be displayed on the button.
     * @param targetPageId The ID of the dialogue page to navigate to when this option is clicked. If null, closes dialogue.
     * @param isVisible A lambda determining if this button should be rendered on the screen.
     * @param isSelectable A lambda determining if this button can be clicked (if false, it appears grayed out).
     * @param action An optional lambda function to be executed when the player clicks this option.
     * @return This [LuxChoicePageBuilder] instance for method chaining.
     */
    @JvmOverloads
    fun option(
        text: String,
        targetPageId: String? = null,
        isVisible: (ActiveDialogue) -> Boolean = { true },
        isSelectable: (ActiveDialogue) -> Boolean = { true },
        action: ((ServerPlayer, ActiveDialogue) -> Unit)? = null
    ): ChoicePageBuilder {

        val optionValue = text.lowercase().replace(" ", "_")

        val dialogueOption = DialogueOption(
            text = WrappedDialogueText(text.text()),
            value = optionValue,
            isVisible = FunctionDialoguePredicate(isVisible),
            isSelectable = FunctionDialoguePredicate(isSelectable),
            action = FunctionDialogueAction { activeDialogue, _ ->
                val nextPage = activeDialogue.dialogueReference.pages.find { it.id == targetPageId }

                if (nextPage != null) {
                    activeDialogue.setPage(nextPage)
                } else {
                    activeDialogue.close()
                }

                // Execute developer's custom action
                action?.invoke(activeDialogue.playerEntity, activeDialogue)
            }
        )

        options.add(dialogueOption)
        return this
    }

    /**
     * Helper function to safely parse color names into Hex strings expected by Cobblemon.
     */
    private fun parseColorToHex(color: String?): String? {
        if (color == null) return null
        return when (color.lowercase()) {
            "black" -> "000000"
            "dark_blue" -> "0000AA"
            "dark_green" -> "00AA00"
            "dark_aqua" -> "00AAAA"
            "dark_red" -> "AA0000"
            "dark_purple" -> "AA00AA"
            "gold" -> "FFAA00"
            "gray" -> "AAAAAA"
            "dark_gray" -> "555555"
            "blue" -> "5555FF"
            "green" -> "55FF55"
            "aqua" -> "55FFFF"
            "red" -> "FF5555"
            "light_purple" -> "FF55FF"
            "yellow" -> "FFFF55"
            "white" -> "FFFFFF"
            else -> color.replace("#", "")
        }
    }

    /**
     * Internal function to construct the final [DialoguePage] from the configured options.
     *
     * @return The fully constructed [DialoguePage] with all its choices and properties.
     */
    internal fun build(): DialoguePage {
        val input = DialogueOptionSetInput(options, vertical = verticalLayout)
        return DialoguePage.of(
            id = id,
            speaker = speakerId,
            lines = listOf(text.text()),
            textColor = parseColorToHex(textColor),
            input = input,
            gibber = gibber,
            background = background
        )
    }
}