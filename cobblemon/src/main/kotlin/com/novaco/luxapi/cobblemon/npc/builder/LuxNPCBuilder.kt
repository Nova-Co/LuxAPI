package com.novaco.luxapi.cobblemon.npc

import com.cobblemon.mod.common.CobblemonEntities
import com.cobblemon.mod.common.api.npc.NPCClass
import com.cobblemon.mod.common.api.npc.NPCClasses
import com.cobblemon.mod.common.api.npc.NPCPartyProvider
import com.cobblemon.mod.common.api.npc.NPCPresets
import com.cobblemon.mod.common.api.npc.configuration.NPCInteractConfiguration
import com.cobblemon.mod.common.api.npc.configuration.interaction.DialogueNPCInteractionConfiguration
import com.cobblemon.mod.common.api.pokemon.PokemonProperties
import com.cobblemon.mod.common.api.npc.partyproviders.SimplePartyProvider
import com.cobblemon.mod.common.entity.npc.NPCEntity
import com.novaco.luxapi.commons.player.LuxPlayer
import com.novaco.luxapi.cobblemon.npc.manager.LuxNPCManager
import com.novaco.luxapi.cobblemon.npc.party.LuxPartyBuilder
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.phys.Vec3
import java.util.concurrent.CompletableFuture

/**
 * A highly fluent builder for creating versatile NPCs in Cobblemon.
 * Abstracts Cobblemon's complex NPC properties, battle configurations, and interactions.
 */
class LuxNPCBuilder(private val spawner: LuxPlayer) {

    // Appearance
    private var npcName: Component = Component.literal("Villager")
    private var skinUsername: String? = null

    // Core
    private var customId: String? = null
    private val extraAspects: MutableSet<String> = mutableSetOf()
    private val nativeBehaviors: MutableList<ResourceLocation> = mutableListOf()

    // Class & Preset
    private var npcClassId: String = "cobblemon:standard"
    private var npcPresetId: String? = null
    private var partyProvider: NPCPartyProvider? = null

    // Behaviors & Appearance
    private var movementType: LuxMovement = LuxMovement.STATIONARY
    private var lookAtPlayer: Boolean = true
    private var isPersistent: Boolean = true
    private var hideNameTag: Boolean = false
    private var renderScale: Float = 1.0f

    // Battle & Party
    private val trainerParty: MutableList<PokemonProperties> = mutableListOf()
    private var canChallenge: Boolean = false
    private var battleTheme: ResourceLocation? = null

    // Interaction
    private var interactionConfiguration: NPCInteractConfiguration? = null
    private var registeredInteractionId: String? = null

    // --- Configuration Methods ---

    fun id(id: String): LuxNPCBuilder { this.customId = id; return this }
    fun name(name: String): LuxNPCBuilder { this.npcName = Component.literal(name); return this }
    fun skin(username: String): LuxNPCBuilder { this.skinUsername = username; return this }
    fun addAspects(vararg aspects: String): LuxNPCBuilder { this.extraAspects.addAll(aspects); return this }
    fun scale(scale: Float): LuxNPCBuilder { this.renderScale = scale; return this }
    fun hideNameTag(hide: Boolean = true): LuxNPCBuilder { this.hideNameTag = hide; return this }
    fun lookAtPlayer(look: Boolean = true): LuxNPCBuilder { this.lookAtPlayer = look; return this }
    fun movement(movement: LuxMovement): LuxNPCBuilder { this.movementType = movement; return this }
    fun persistent(persistent: Boolean = true): LuxNPCBuilder { this.isPersistent = persistent; return this }
    fun npcClass(classId: String): LuxNPCBuilder { this.npcClassId = classId; return this }
    fun preset(presetId: String): LuxNPCBuilder { this.npcPresetId = presetId; return this }

    fun party(setup: LuxPartyBuilder.() -> Unit): LuxNPCBuilder {
        val partyBuilder = LuxPartyBuilder()
        partyBuilder.setup()
        this.partyProvider = partyBuilder.build()
        return this
    }

    fun addPokemon(spec: String): LuxNPCBuilder {
        if (trainerParty.size < 6) trainerParty.add(PokemonProperties.parse(spec))
        return this
    }

    fun addPokemon(properties: PokemonProperties): LuxNPCBuilder {
        if (trainerParty.size < 6) trainerParty.add(properties)
        return this
    }

    fun enableBattle(themePath: String? = null): LuxNPCBuilder {
        this.canChallenge = true
        if (themePath != null) this.battleTheme = ResourceLocation.parse(themePath)
        return this
    }

    /**
     * Binds native Cobblemon AI behaviors to this NPC (e.g., "cobblemon:healer_behavior").
     */
    fun addNativeBehavior(behaviorId: String): LuxNPCBuilder {
        this.nativeBehaviors.add(ResourceLocation.parse(behaviorId))
        return this
    }

    /**
     * Loads a native Cobblemon JSON dialogue from a Datapack.
     */
    fun nativeDialogue(dialoguePath: String): LuxNPCBuilder {
        val config = DialogueNPCInteractionConfiguration()
        config.dialogue = ResourceLocation.parse(dialoguePath)
        this.interactionConfiguration = config
        return this
    }

    /**
     * Dynamically handles interactions using Kotlin Code.
     * (Overrides native dialogues if both are set).
     */
    fun onInteract(action: (player: ServerPlayer, npc: NPCEntity) -> Unit): LuxNPCBuilder {
        this.interactionConfiguration = LuxCustomInteraction(action)
        return this
    }

    /**
     * Binds this NPC to a globally registered interaction handler.
     * This interaction WILL survive server restarts.
     *
     * @param interactId The ID previously registered in LuxInteractionRegistry.
     */
    fun bindInteraction(interactId: String): LuxNPCBuilder {
        this.registeredInteractionId = interactId
        return this
    }

    // --- Spawning Logic ---

    fun spawn(): NPCEntity? {
        val serverPlayer = spawner.parent as ServerPlayer
        val serverLevel = serverPlayer.serverLevel()

        val flatLookVector = Vec3.directionFromRotation(0.0f, serverPlayer.yRot)
        val spawnPos = serverPlayer.position().add(flatLookVector.scale(2.0))

        val npcEntity = CobblemonEntities.NPC.create(serverLevel) ?: return null

        // 1. Assign Native Class & Preset safely via Cloning
        val baseClass = NPCClasses.getByIdentifier(ResourceLocation.parse(npcClassId)) ?: NPCClasses.dummy()
        val npcClassInst = cloneNPCClass(baseClass)

        npcPresetId?.let { presetId ->
            NPCPresets.getPreset(ResourceLocation.parse(presetId))?.applyTo(npcClassInst)
        }

        npcEntity.npc = npcClassInst

        // 2. Core Visuals & Scaling
        npcEntity.customName = npcName
        npcEntity.isCustomNameVisible = !hideNameTag
        npcEntity.hideNameTag = hideNameTag
        npcEntity.renderScale = renderScale
        npcEntity.hitboxScale = renderScale

        // 3. Apply AI Movement & Behaviors
        npcEntity.isMovable = (movementType == LuxMovement.WANDER)

        if (nativeBehaviors.isNotEmpty()) {
            npcEntity.behavioursAreCustom = true
            npcEntity.behaviours.addAll(nativeBehaviors)
        }

        if (isPersistent) npcEntity.setPersistenceRequired()

        // 4. Apply Aspects
        if (extraAspects.isNotEmpty()) {
            npcEntity.appliedAspects.addAll(extraAspects)
            npcEntity.updateAspects()
        }

        // 5. Position & Rotation
        npcEntity.setPos(spawnPos.x, spawnPos.y, spawnPos.z)
        npcEntity.yRot = serverPlayer.yRot + 180.0f
        npcEntity.yHeadRot = npcEntity.yRot

        // 6. Battle & Party Setup
        if (partyProvider != null) {
            npcEntity.npc.party = partyProvider
            npcEntity.party = partyProvider?.provide(npcEntity, npcEntity.level)
        } else if (trainerParty.isNotEmpty()) {
            val simpleProvider = SimplePartyProvider()
            trainerParty.forEach { simpleProvider.pokemon.add(it) }
            npcEntity.npc.party = simpleProvider
            npcEntity.party = simpleProvider.provide(npcEntity, npcEntity.level)
        }

        if (canChallenge) {
            npcEntity.getBattleConfiguration().canChallenge = true
            if (battleTheme != null) {
                npcEntity.npc.battleTheme = battleTheme
            }
        }

        // 7. Inject Interactions
        if (interactionConfiguration != null) {
            npcEntity.interaction = interactionConfiguration
        }

        // Inject persistent Interaction Tag <---
        if (registeredInteractionId != null) {
            npcEntity.addTag("lux_interact:$registeredInteractionId")
        }

        // 8. Spawn & Finalize
        val success = serverLevel.addFreshEntity(npcEntity)
        if (success) {
            if (lookAtPlayer) {
                npcEntity.addTag("lux_look_at_interactor")
                com.novaco.luxapi.cobblemon.npc.tracker.LuxNPCTracker.register(npcEntity.uuid)
            }

            customId?.let { id ->
                LuxNPCManager.registerNPC(id, npcEntity.uuid)
            }

            skinUsername?.let { username ->
                CompletableFuture.runAsync {
                    try {
                        npcEntity.loadTextureFromGameProfileName(username)
                    } catch (e: Exception) {
                        println("[LuxAPI] Failed to load skin for $username: ${e.message}")
                    }
                }
            }
        }

        return if (success) npcEntity else null
    }

    private fun cloneNPCClass(original: NPCClass): NPCClass {
        val clone = NPCClass()
        clone.id = original.id
        clone.resourceIdentifier = original.resourceIdentifier
        clone.names = original.names.toMutableList()
        clone.aspects = original.aspects.toMutableSet()
        clone.hitbox = original.hitbox
        clone.modelScale = original.modelScale
        clone.battleConfiguration = original.battleConfiguration
        clone.interaction = original.interaction
        clone.canDespawn = original.canDespawn
        clone.variations = original.variations.toMutableMap()
        clone.config = original.config.toMutableList()
        clone.variables = original.variables.toMutableMap()
        clone.party = original.party
        clone.skill = original.skill
        clone.autoHealParty = original.autoHealParty
        clone.randomizePartyOrder = original.randomizePartyOrder
        clone.battleTheme = original.battleTheme
        clone.behaviours = original.behaviours.toMutableList()
        clone.isMovable = original.isMovable
        clone.isInvulnerable = original.isInvulnerable
        clone.isLeashable = original.isLeashable
        clone.allowProjectileHits = original.allowProjectileHits
        clone.hideNameTag = original.hideNameTag
        return clone
    }

    private class LuxCustomInteraction(
        private val action: (ServerPlayer, NPCEntity) -> Unit
    ) : NPCInteractConfiguration {
        override val type: String = "lux_custom"
        override fun interact(npc: NPCEntity, player: ServerPlayer): Boolean {
            action(player, npc)
            return true
        }
        override fun encode(buffer: RegistryFriendlyByteBuf) {}
        override fun decode(buffer: RegistryFriendlyByteBuf) {}
        override fun writeToNBT(compoundTag: CompoundTag) {}
        override fun readFromNBT(compoundTag: CompoundTag) {}
        override fun isDifferentTo(other: NPCInteractConfiguration) = true
    }
}