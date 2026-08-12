package com.novaco.luxapi.cobblemon.dialogue

import com.cobblemon.mod.common.api.dialogue.DialoguePage
import com.cobblemon.mod.common.api.dialogue.FunctionDialogueAction
import com.cobblemon.mod.common.api.dialogue.WrappedDialogueText
import com.cobblemon.mod.common.api.dialogue.input.DialogueOption
import com.cobblemon.mod.common.api.dialogue.input.DialogueOptionSetInput
import com.cobblemon.mod.common.api.dialogue.input.DialogueTextInput
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
    private var isInputMode = false
    private var inputPlaceholder: String = ""
    private var inputCallback: ((ServerPlayer, ActiveDialogue, String) -> Unit)? = null
    private var textInputNextPageId: String? = null

    var verticalLayout: Boolean = false
    var textColor: String? = null
    var gibber: DialogueGibber? = null
    var background: ResourceLocation? = null
    var escapeAction: ((ActiveDialogue) -> Unit)? = null

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
        if (isInputMode) throw IllegalStateException("Cannot bind standard choices on a page configured for Text Field input!")

        val optionValue = text.lowercase().replace(" ", "_")
        val dialogueOption = DialogueOption(
            text = WrappedDialogueText(text.text()),
            value = optionValue,
            isVisible = FunctionDialoguePredicate(isVisible),
            isSelectable = FunctionDialoguePredicate(isSelectable),
            action = FunctionDialogueAction { activeDialogue, _ ->
                val nextPage = activeDialogue.dialogueReference.pages.find { it.id == targetPageId }
                if (nextPage != null) activeDialogue.setPage(nextPage) else activeDialogue.close()
                action?.invoke(activeDialogue.playerEntity, activeDialogue)
            }
        )
        options.add(dialogueOption)
        return this
    }

    /**
     * Registers and embeds a direct TextInput box component safely inside this page.
     * Complete with a built-in input text sanitizer layer.
     *
     * @param placeholder Text string showing inside the field beforehand.
     * @param targetPageId Navigation destination following submission.
     * @param onSubmit Callback process capturing the input value cleanly.
     */
    fun appendInputField(
        placeholder: String,
        targetPageId: String? = null,
        onSubmit: (ServerPlayer, ActiveDialogue, String) -> Unit
    ): ChoicePageBuilder {
        this.isInputMode = true
        this.inputPlaceholder = placeholder
        this.textInputNextPageId = targetPageId
        this.inputCallback = onSubmit
        return this
    }

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

    internal fun build(): DialoguePage {
        val inputComponent = if (isInputMode) {
            DialogueTextInput().apply {
                // Attach placeholder text natively
                action = FunctionDialogueAction { dialogue, capturedString ->
                    // SECURE INJECTION CHECK: Strip away potential exploit characters
                    val sanitized = (capturedString ?: "").replace(Regex("[^a-zA-Z0-9_\\-\\s]"), "").trim()

                    inputCallback?.invoke(dialogue.playerEntity, dialogue, sanitized)

                    val nextPage = dialogue.dialogueReference.pages.find { it.id == textInputNextPageId }
                    if (nextPage != null) dialogue.setPage(nextPage) else dialogue.close()
                }
            }
        } else {
            DialogueOptionSetInput(options, vertical = verticalLayout)
        }

        return DialoguePage.of(
            id = id,
            speaker = speakerId,
            lines = listOf(text.text()),
            textColor = parseColorToHex(textColor),
            input = inputComponent,
            gibber = gibber,
            background = background,
            escapeAction = escapeAction
        )
    }
}