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
import com.novaco.luxapi.cobblemon.npc.battle.LuxBattleRegistry
import com.novaco.luxapi.cobblemon.npc.battle.LuxBattleResult
import com.novaco.luxapi.commons.player.LuxPlayer
import com.novaco.luxapi.cobblemon.npc.manager.LuxInteractionRegistry
import com.novaco.luxapi.cobblemon.npc.manager.LuxNPCManager
import com.novaco.luxapi.cobblemon.npc.party.LuxPartyBuilder
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.phys.Vec3
import java.util.UUID
import java.util.concurrent.CompletableFuture

/**
 * A fluent builder for creating and configuring Cobblemon NPCs with a wide range of features.
 * This builder simplifies the complexities of Cobblemon's native NPC system, providing a more
 * intuitive way to define appearance, behavior, party, and interactions.
 *
 * @param spawner The player who is initiating the creation of this NPC.
 */
class LuxNPCBuilder(private val spawner: LuxPlayer) {

    private var npcName: Component = Component.literal("Villager")
    private var skinUsername: String? = null
    private var customId: String? = null
    private val extraAspects: MutableSet<String> = mutableSetOf()
    private val nativeBehaviors: MutableList<ResourceLocation> = mutableListOf()
    private var npcClassId: String = "cobblemon:standard"
    private var npcPresetId: String? = null
    private var partyProvider: NPCPartyProvider? = null
    private var movementType: LuxMovement = LuxMovement.STATIONARY
    private var lookAtPlayer: Boolean = true
    private var isPersistent: Boolean = true
    private var hideNameTag: Boolean = false
    private var renderScale: Float = 1.0f
    private val trainerParty: MutableList<PokemonProperties> = mutableListOf()
    private var canChallenge: Boolean = false
    private var battleTheme: ResourceLocation? = null
    private var interactionConfiguration: NPCInteractConfiguration? = null
    private var registeredInteractionId: String? = null

    private var battleEndAction: ((ServerPlayer, NPCEntity, LuxBattleResult) -> Unit)? = null

    /** Sets a custom ID for tracking the NPC via [LuxNPCManager]. */
    fun id(id: String): LuxNPCBuilder { this.customId = id; return this }

    /** Sets the display name of the NPC. */
    fun name(name: String): LuxNPCBuilder { this.npcName = Component.literal(name); return this }

    /** Sets the NPC's skin to that of a Minecraft player. */
    fun skin(username: String): LuxNPCBuilder { this.skinUsername = username; return this }

    /** Adds one or more visual aspects to the NPC (e.g., "shiny"). */
    fun addAspects(vararg aspects: String): LuxNPCBuilder { this.extraAspects.addAll(aspects); return this }

    /** Sets the render scale of the NPC. */
    fun scale(scale: Float): LuxNPCBuilder { this.renderScale = scale; return this }

    /** Hides the NPC's name tag. */
    fun hideNameTag(hide: Boolean = true): LuxNPCBuilder { this.hideNameTag = hide; return this }

    /** Makes the NPC look at nearby players. */
    fun lookAtPlayer(look: Boolean = true): LuxNPCBuilder { this.lookAtPlayer = look; return this }

    /** Defines the NPC's movement behavior. */
    fun movement(movement: LuxMovement): LuxNPCBuilder { this.movementType = movement; return this }

    /** If true, the NPC will not despawn naturally. */
    fun persistent(persistent: Boolean = true): LuxNPCBuilder { this.isPersistent = persistent; return this }

    /** Sets the base NPC class (e.g., "cobblemon:trainer"). */
    fun npcClass(classId: String): LuxNPCBuilder { this.npcClassId = classId; return this }

    /** Applies a predefined NPC preset from data packs. */
    fun preset(presetId: String): LuxNPCBuilder { this.npcPresetId = presetId; return this }

    /** Configures the NPC's party using a dedicated builder. */
    fun party(setup: LuxPartyBuilder.() -> Unit): LuxNPCBuilder {
        val partyBuilder = LuxPartyBuilder()
        partyBuilder.setup()
        this.partyProvider = partyBuilder.build()
        return this
    }

    /** Adds a Pokémon to the NPC's party using a spec string (e.g., "pikachu level=10"). */
    fun addPokemon(spec: String): LuxNPCBuilder {
        if (trainerParty.size < 6) trainerParty.add(PokemonProperties.parse(spec))
        return this
    }

    /** Adds a Pokémon to the NPC's party using a [PokemonProperties] object. */
    fun addPokemon(properties: PokemonProperties): LuxNPCBuilder {
        if (trainerParty.size < 6) trainerParty.add(properties)
        return this
    }

    /** Enables battling against this NPC and optionally sets a battle theme. */
    fun enableBattle(themePath: String? = null): LuxNPCBuilder {
        this.canChallenge = true
        if (themePath != null) this.battleTheme = ResourceLocation.parse(themePath)
        return this
    }

    /** Adds a native Cobblemon AI behavior (e.g., "cobblemon:healer_behavior"). */
    fun addNativeBehavior(behaviorId: String): LuxNPCBuilder {
        this.nativeBehaviors.add(ResourceLocation.parse(behaviorId))
        return this
    }

    /** Loads a native Cobblemon JSON dialogue from a data pack. */
    fun nativeDialogue(dialoguePath: String): LuxNPCBuilder {
        val config = DialogueNPCInteractionConfiguration()
        config.dialogue = ResourceLocation.parse(dialoguePath)
        this.interactionConfiguration = config
        return this
    }

    /** * Defines a custom, dynamic interaction in Kotlin code.
     * Automatically hijacks the interaction via LuxInteractionRegistry to bypass native auto-battles.
     */
    fun onInteract(action: (player: ServerPlayer, npc: NPCEntity) -> Unit): LuxNPCBuilder {
        val uniqueId = UUID.randomUUID().toString()
        this.registeredInteractionId = uniqueId
        LuxInteractionRegistry.register(uniqueId, action)
        return this
    }

    /** Binds this NPC to a globally registered interaction that persists across server restarts. */
    fun bindInteraction(interactId: String): LuxNPCBuilder {
        this.registeredInteractionId = interactId
        return this
    }

    /**
     * Defines a custom callback to be executed when a battle with this NPC concludes.
     *
     * @param action The logic to run, providing the player, the NPC, and the battle outcome.
     * @return This builder instance for method chaining.
     */
    fun onBattleEnd(action: (ServerPlayer, NPCEntity, LuxBattleResult) -> Unit): LuxNPCBuilder {
        this.battleEndAction = action
        return this
    }

    /**
     * Constructs and spawns the NPC in the world in front of the spawner.
     * @return The spawned [NPCEntity], or null if spawning failed.
     */
    fun spawn(): NPCEntity? {
        val serverPlayer = spawner.parent as ServerPlayer
        val serverLevel = serverPlayer.serverLevel()

        val flatLookVector = Vec3.directionFromRotation(0.0f, serverPlayer.yRot)
        val spawnPos = serverPlayer.position().add(flatLookVector.scale(2.0))

        val npcEntity = CobblemonEntities.NPC.create(serverLevel) ?: return null

        val baseClass = NPCClasses.getByIdentifier(ResourceLocation.parse(npcClassId))
            ?: NPCClasses.getByName("standard")
            ?: NPCClasses.dummy()

        val npcClassInst = cloneNPCClass(baseClass)
        npcPresetId?.let { NPCPresets.getPreset(ResourceLocation.parse(it))?.applyTo(npcClassInst) }
        npcEntity.npc = npcClassInst

        npcEntity.customName = npcName
        npcEntity.isCustomNameVisible = !hideNameTag
        npcEntity.hideNameTag = hideNameTag
        npcEntity.renderScale = renderScale
        npcEntity.hitboxScale = renderScale

        npcEntity.isMovable = (movementType == LuxMovement.WANDER)
        if (nativeBehaviors.isNotEmpty()) {
            npcEntity.behavioursAreCustom = true
            npcEntity.behaviours.addAll(nativeBehaviors)
        }
        if (isPersistent) npcEntity.setPersistenceRequired()

        if (extraAspects.isNotEmpty()) {
            npcEntity.appliedAspects.addAll(extraAspects)
            npcEntity.updateAspects()
        }

        npcEntity.setPos(spawnPos.x, spawnPos.y, spawnPos.z)
        npcEntity.yRot = serverPlayer.yRot + 180.0f
        npcEntity.yHeadRot = npcEntity.yRot

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
            if (battleTheme != null) npcEntity.npc.battleTheme = battleTheme
        } else {
            // Explicitly disable native auto-challenging to protect dialogue flows
            npcEntity.getBattleConfiguration().canChallenge = false
        }

        if (interactionConfiguration != null) npcEntity.interaction = interactionConfiguration
        if (registeredInteractionId != null) npcEntity.addTag("lux_interact:$registeredInteractionId")

        val success = serverLevel.addFreshEntity(npcEntity)
        if (success) {
            if (lookAtPlayer) {
                npcEntity.addTag("lux_look_at_interactor")
                com.novaco.luxapi.cobblemon.npc.tracker.LuxNPCTracker.register(npcEntity.uuid)
            }
            customId?.let { LuxNPCManager.registerNPC(it, npcEntity.uuid) }

            battleEndAction?.let { action ->
                LuxBattleRegistry.register(npcEntity.uuid, action)
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

    /** Creates a deep clone of an NPCClass to prevent modifying the original registry. */
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
}