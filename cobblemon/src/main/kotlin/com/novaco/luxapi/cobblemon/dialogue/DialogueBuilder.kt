package com.novaco.luxapi.cobblemon.dialogue

import com.cobblemon.mod.common.api.dialogue.*
import com.cobblemon.mod.common.api.dialogue.input.*
import com.cobblemon.mod.common.api.text.text
import com.cobblemon.mod.common.entity.npc.NPCEntity
import com.novaco.luxapi.commons.player.LuxPlayer
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer
import java.util.UUID

/**
 * A comprehensive fluent builder for constructing complex, multi-page Cobblemon dialogues.
 * Supports standard text pages, choice matrices, text inputs, auto-advancing pages, and custom speakers.
 */
class DialogueBuilder {

    private val pages = mutableListOf<DialoguePage>()
    private val speakers = mutableMapOf<String, DialogueSpeaker>()
    private var defaultBackground: ResourceLocation = Dialogue.DEFAULT_BACKGROUND
    private var initAction: ((ActiveDialogue) -> Unit)? = null
    private var escapeActionCallback: (ActiveDialogue) -> Unit = { it.close() }

    /**
     * Registers a standard text-only speaker.
     *
     * @param id A unique identifier for this speaker.
     * @param name The name to be displayed in the dialogue box.
     * @param gibber Optional typing sound effect properties.
     * @return This [DialogueBuilder] instance for chaining.
     */
    @JvmOverloads
    fun addSpeaker(id: String, name: String, gibber: DialogueGibber? = null): DialogueBuilder {
        speakers[id] = DialogueSpeaker().of(name = name.text(), gibber = gibber)
        return this
    }

    /**
     * Registers a speaker that utilizes a player's skin as their dialogue portrait.
     *
     * @param id A unique identifier for this speaker.
     * @param name The name to be displayed.
     * @param playerUuid The UUID of the player whose face will be rendered.
     * @param gibber Optional typing sound effect properties.
     * @return This [DialogueBuilder] instance for chaining.
     */
    @JvmOverloads
    fun addPlayerSpeaker(id: String, name: String, playerUuid: UUID, gibber: DialogueGibber? = null): DialogueBuilder {
        speakers[id] = DialogueSpeaker().of(
            name = name.text(),
            face = PlayerDialogueFaceProvider(playerUuid),
            gibber = gibber
        )
        return this
    }

    /**
     * Registers a speaker that utilizes an NPC's model as their dialogue portrait.
     *
     * @param id A unique identifier for this speaker.
     * @param name The name to be displayed.
     * @param npcId The entity ID of the NPC to render.
     * @param gibber Optional typing sound effect properties.
     * @return This [DialogueBuilder] instance for chaining.
     */
    @JvmOverloads
    fun addNpcSpeaker(id: String, name: String, npcId: Int, gibber: DialogueGibber? = null): DialogueBuilder {
        speakers[id] = DialogueSpeaker().of(
            name = name.text(),
            face = ReferenceDialogueFaceProvider(npcId),
            gibber = gibber
        )
        return this
    }

    /**
     * Registers a speaker that renders a Pokémon species' model as their dialogue portrait
     * (Cobblemon's "artificial" face provider — no live entity required).
     *
     * @param id A unique identifier for this speaker.
     * @param name The name to be displayed.
     * @param speciesId The species resource location (e.g. "cobblemon:bulbasaur").
     * @param aspects Optional visual aspects to apply (e.g. "shiny").
     * @param gibber Optional typing sound effect properties.
     * @return This [DialogueBuilder] instance for chaining.
     */
    @JvmOverloads
    fun addPokemonSpeaker(id: String, name: String, speciesId: String, aspects: Set<String> = emptySet(), gibber: DialogueGibber? = null): DialogueBuilder {
        speakers[id] = DialogueSpeaker().of(
            name = name.text(),
            face = ArtificialDialogueFaceProvider(
                modelType = "pokemon",
                identifier = ResourceLocation.parse(speciesId),
                aspects = aspects
            ),
            gibber = gibber
        )
        return this
    }

    /**
     * Adds a standard dialogue page where the player clicks to advance.
     *
     * @param id A unique identifier for this page.
     * @param speakerId The ID of the registered speaker for this page.
     * @param text The text content.
     * @param nextPageId The ID of the page to navigate to next. If null, closes the dialogue.
     * @param timeoutSeconds Optional time in seconds before the page auto-closes (or triggers timeout action).
     * @param onEnter Optional action executed when the page is displayed.
     * @param onTimeout Optional action executed if the page times out (defaults to closing the dialogue).
     * @param escapeAction Optional override for what happens when the player presses ESC on this page
     *   (falls back to the dialogue-level one set via [onEscape] if null).
     * @return This [DialogueBuilder] instance for chaining.
     */
    @JvmOverloads
    fun addPage(
        id: String,
        speakerId: String? = null,
        text: String,
        nextPageId: String? = null,
        timeoutSeconds: Float? = null,
        onEnter: ((ActiveDialogue) -> Unit)? = null,
        onTimeout: ((ActiveDialogue) -> Unit)? = null,
        escapeAction: ((ActiveDialogue) -> Unit)? = null
    ): DialogueBuilder {
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
            if (timeoutSeconds != null && timeoutSeconds > 0) {
                timeout = DialogueTimeout(
                    duration = timeoutSeconds,
                    action = onTimeout?.let { cb -> FunctionDialogueAction { dialogue, _ -> cb(dialogue) } }
                        ?: FunctionDialogueAction { dialogue, _ -> dialogue.close() }
                )
            }
        }
        pages.add(
            DialoguePage.of(
                id = id,
                speaker = speakerId,
                lines = listOf(text.text()),
                input = input,
                escapeAction = escapeAction
            )
        )
        return this
    }

    /**
     * Adds a page whose text is computed dynamically each time it's rendered, instead of a fixed string.
     *
     * @param id A unique identifier for this page.
     * @param speakerId The ID of the registered speaker for this page.
     * @param textProvider A function producing the page text from the current [ActiveDialogue] state.
     * @param nextPageId The ID of the page to navigate to next. If null, closes the dialogue.
     * @param timeoutSeconds Optional time in seconds before the page auto-closes (or triggers timeout action).
     * @param onEnter Optional action executed when the page is displayed.
     * @param onTimeout Optional action executed if the page times out (defaults to closing the dialogue).
     * @param escapeAction Optional override for what happens when the player presses ESC on this page.
     * @return This [DialogueBuilder] instance for chaining.
     */
    @JvmOverloads
    fun addDynamicPage(
        id: String,
        speakerId: String? = null,
        textProvider: (ActiveDialogue) -> String,
        nextPageId: String? = null,
        timeoutSeconds: Float? = null,
        onEnter: ((ActiveDialogue) -> Unit)? = null,
        onTimeout: ((ActiveDialogue) -> Unit)? = null,
        escapeAction: ((ActiveDialogue) -> Unit)? = null
    ): DialogueBuilder {
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
            if (timeoutSeconds != null && timeoutSeconds > 0) {
                timeout = DialogueTimeout(
                    duration = timeoutSeconds,
                    action = onTimeout?.let { cb -> FunctionDialogueAction { dialogue, _ -> cb(dialogue) } }
                        ?: FunctionDialogueAction { dialogue, _ -> dialogue.close() }
                )
            }
        }
        pages.add(
            DialoguePage(
                id = id,
                speaker = speakerId,
                lines = mutableListOf(FunctionDialogueText { ad -> textProvider(ad).text() }),
                input = input,
                escapeAction = escapeAction?.let { cb -> FunctionDialogueAction { ad, _ -> cb(ad) } }
            )
        )
        return this
    }

    /**
     * Adds a page with a text input field, allowing players to type a response.
     *
     * @param id A unique identifier for this page.
     * @param speakerId The ID of the registered speaker for this page.
     * @param text The prompt text presented above the input field.
     * @param nextPageId The ID of the page to navigate to next.
     * @param onInput Action executed upon submission, providing the typed string.
     * @return This [DialogueBuilder] instance for chaining.
     */
    @JvmOverloads
    fun addInputPage(
        id: String,
        speakerId: String? = null,
        text: String,
        placeholder: String = "Type here...",
        nextPageId: String? = null,
        onInput: (ServerPlayer, ActiveDialogue, String) -> Unit
    ): DialogueBuilder {
        return addChoicePage(id, speakerId, text) {
            appendInputField(placeholder, nextPageId, onInput)
        }
    }

    /**
     * Adds a page that automatically advances to the next one after a set delay.
     *
     * @param id A unique identifier for this page.
     * @param speakerId The ID of the registered speaker for this page.
     * @param text The text content.
     * @param showTimer Whether to display a visual timer UI element.
     * @return This [DialogueBuilder] instance for chaining.
     */
    @JvmOverloads
    fun addAutoPage(
        id: String,
        speakerId: String? = null,
        text: String,
        showTimer: Boolean = false
    ): DialogueBuilder {
        val input = DialogueAutoContinueInput().apply {
            this.showTimer = showTimer
        }

        pages.add(DialoguePage.of(id = id, speaker = speakerId, lines = listOf(text.text()), input = input))
        return this
    }

    /**
     * Adds a page that presents multiple choices to the player.
     *
     * @param id A unique identifier for this page.
     * @param speakerId The ID of the registered speaker for this page.
     * @param text The question or prompt text.
     * @param setup A configuration lambda for [ChoicePageBuilder].
     * @return This [DialogueBuilder] instance for chaining.
     */
    @JvmOverloads
    fun addChoicePage(
        id: String,
        speakerId: String? = null,
        text: String,
        setup: ChoicePageBuilder.() -> Unit
    ): DialogueBuilder {
        val choiceBuilder = ChoicePageBuilder(id, speakerId, text)
        choiceBuilder.setup()
        pages.add(choiceBuilder.build())
        return this
    }

    /**
     * Sets a custom background texture for the dialogue UI.
     *
     * @param texturePath The resource path (e.g., "cobblemon:textures/gui/dialogue/custom.png").
     * @return This [DialogueBuilder] instance for chaining.
     */
    fun setBackground(texturePath: String): DialogueBuilder {
        this.defaultBackground = ResourceLocation.parse(texturePath)
        return this
    }

    /**
     * Sets an action to run once when the dialogue is first initialized, before the first page renders.
     *
     * @return This [DialogueBuilder] instance for chaining.
     */
    fun onInitialize(action: (ActiveDialogue) -> Unit): DialogueBuilder {
        this.initAction = action
        return this
    }

    /**
     * Overrides what happens when the player presses ESC, at the dialogue level. Applies to any page
     * that doesn't set its own page-level `escapeAction`. Defaults to closing the dialogue if never called.
     *
     * @return This [DialogueBuilder] instance for chaining.
     */
    fun onEscape(action: (ActiveDialogue) -> Unit): DialogueBuilder {
        this.escapeActionCallback = action
        return this
    }

    private fun buildDialogue(): Dialogue {
        return Dialogue(
            pages = pages,
            background = defaultBackground,
            escapeAction = FunctionDialogueAction { ad, _ -> escapeActionCallback(ad) },
            speakers = speakers,
            initializationAction = FunctionDialogueAction { ad, _ -> initAction?.invoke(ad) }
        )
    }

    /**
     * Finalizes the dialogue construction and opens it for the player.
     *
     * @param player The target [LuxPlayer].
     * @param npc An optional [NPCEntity] context to associate with the dialogue.
     * @return The resulting [ActiveDialogue] session.
     */
    @JvmOverloads
    fun buildAndOpen(player: LuxPlayer, npc: NPCEntity? = null): ActiveDialogue {
        val serverPlayer = player.parent as ServerPlayer
        val dialogue = buildDialogue()
        return if (npc != null) {
            DialogueManager.startDialogue(serverPlayer, npc, dialogue)
        } else {
            DialogueManager.startDialogue(serverPlayer, dialogue)
        }
    }

    /**
     * Builds the dialogue and registers it into Cobblemon's own dialogue registry under [id], so it can
     * be referenced the same way a datapack-authored dialogue would be — including from
     * [com.novaco.luxapi.cobblemon.npc.NPCBuilder.dynamicDialogue].
     *
     * @param id The resource-location-style ID to register this dialogue under (e.g. "luxapi:my_dialogue").
     * @return The built [Dialogue].
     */
    fun buildAndRegister(id: String): Dialogue {
        val dialogue = buildDialogue()
        Dialogues.dialogues[ResourceLocation.parse(id)] = dialogue
        return dialogue
    }

    companion object {
        /**
         * Opens a dialogue previously registered via [buildAndRegister] (or loaded from a datapack)
         * by its resource-location ID.
         *
         * @return The resulting [ActiveDialogue], or null if no dialogue is registered under [id].
         */
        @JvmOverloads
        fun openRegistered(id: String, player: LuxPlayer, npc: NPCEntity? = null): ActiveDialogue? {
            val dialogue = Dialogues.dialogues[ResourceLocation.parse(id)] ?: return null
            val serverPlayer = player.parent as ServerPlayer
            return if (npc != null) {
                DialogueManager.startDialogue(serverPlayer, npc, dialogue)
            } else {
                DialogueManager.startDialogue(serverPlayer, dialogue)
            }
        }
    }
}
