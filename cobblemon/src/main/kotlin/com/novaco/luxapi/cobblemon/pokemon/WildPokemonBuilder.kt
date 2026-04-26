package com.novaco.luxapi.cobblemon.pokemon

import com.cobblemon.mod.common.CobblemonEntities
import com.cobblemon.mod.common.api.pokemon.PokemonProperties
import com.cobblemon.mod.common.api.pokemon.PokemonSpecies
import com.cobblemon.mod.common.api.pokemon.stats.Stats
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.novaco.luxapi.cobblemon.boss.BossHpSynchronizer
import com.novaco.luxapi.cobblemon.boss.event.BossSpawnEvent
import com.novaco.luxapi.cobblemon.boss.event.LuxBossHooks
import com.novaco.luxapi.cobblemon.boss.phase.BossPhase
import com.novaco.luxapi.cobblemon.boss.phase.BossPhaseManager
import com.novaco.luxapi.cobblemon.listener.UncatchableManager
import com.novaco.luxapi.core.bossbar.BossBarManager
import com.novaco.luxapi.core.bossbar.buildBossBar
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.BossEvent
import net.minecraft.world.entity.ai.attributes.Attributes
import net.minecraft.world.phys.Vec3
import net.minecraft.world.scores.Team

/**
 * A powerful builder for creating and spawning customized Wild Pokémon.
 * Highly useful for generating Boss Pokémon, Quest Targets, or Event Spawns
 * with precise control over stats, appearance, and entity tags.
 */
class WildPokemonBuilder(private val speciesName: String) {

    private var level: Int = 50
    private var shiny: Boolean? = null
    private var customName: String? = null
    private var makeFlawless: Boolean = false
    private val tags = mutableListOf<String>()

    private var immovable: Boolean = false
    private var invulnerable: Boolean = false
    private var bossBarName: String? = null

    private var glowing: Boolean = false
    private var persistent: Boolean = true
    private var knockbackResistant: Boolean = false

    private var bossId: String? = null
    private var uncatchable: Boolean = false
    private var customScale: Float = 1.0f
    private var customMaxHealth: Double? = null

    private var customLootId: String? = null
    private var interactTrigger: String? = null
    private var disableExp: Boolean = false
    private var hostile: Boolean = false
    private var requirePermission: String? = null

    private val customIvs = mutableMapOf<Stats, Int>()
    private val customEvs = mutableMapOf<Stats, Int>()

    private val phases = mutableListOf<BossPhase>()

    /**
     * Sets the level of the spawned Pokémon.
     */
    fun setLevel(level: Int) = apply { this.level = level }

    /**
     * Forces the Pokémon to be shiny (or explicitly not shiny).
     */
    fun setShiny(isShiny: Boolean = true) = apply { this.shiny = isShiny }

    /**
     * Gives the Pokémon a custom display name (e.g., "§c§lThe Volcano Lord").
     */
    fun setCustomName(name: String) = apply { this.customName = name }

    /**
     * Sets specific Individual Values (IVs) using named arguments.
     * Unspecified values will remain untouched. Values are safely capped between 0 and 31.
     */
    fun setIVs(
        hp: Int? = null,
        atk: Int? = null,
        def: Int? = null,
        spatk: Int? = null,
        spdef: Int? = null,
        speed: Int? = null
    ) = apply {
        hp?.let { customIvs[Stats.HP] = it.coerceIn(0, 31) }
        atk?.let { customIvs[Stats.ATTACK] = it.coerceIn(0, 31) }
        def?.let { customIvs[Stats.DEFENCE] = it.coerceIn(0, 31) }
        spatk?.let { customIvs[Stats.SPECIAL_ATTACK] = it.coerceIn(0, 31) }
        spdef?.let { customIvs[Stats.SPECIAL_DEFENCE] = it.coerceIn(0, 31) }
        speed?.let { customIvs[Stats.SPEED] = it.coerceIn(0, 31) }
    }

    /**
     * Sets specific Effort Values (EVs) using named arguments.
     * Unspecified values will remain untouched. Values are safely capped between 0 and 252.
     */
    fun setEVs(
        hp: Int? = null,
        atk: Int? = null,
        def: Int? = null,
        spatk: Int? = null,
        spdef: Int? = null,
        speed: Int? = null
    ) = apply {
        hp?.let { customEvs[Stats.HP] = it.coerceIn(0, 252) }
        atk?.let { customEvs[Stats.ATTACK] = it.coerceIn(0, 252) }
        def?.let { customEvs[Stats.DEFENCE] = it.coerceIn(0, 252) }
        spatk?.let { customEvs[Stats.SPECIAL_ATTACK] = it.coerceIn(0, 252) }
        spdef?.let { customEvs[Stats.SPECIAL_DEFENCE] = it.coerceIn(0, 252) }
        speed?.let { customEvs[Stats.SPEED] = it.coerceIn(0, 252) }
    }

    /**
     * Sets all IVs to their absolute maximum (31).
     */
    fun setFlawless() = apply {
        Stats.values().forEach { customIvs[it] = 31 }
    }

    /**
     * Randomizes all IVs within a specific range. Perfect for unpredictable encounters.
     */
    fun randomizeIVs(min: Int = 0, max: Int = 31) = apply {
        Stats.values().forEach { customIvs[it] = (min..max).random() }
    }

    /**
     * Adds a specific Minecraft Entity Tag.
     * Highly recommended for tracking Boss deaths or applying custom drops.
     */
    fun addTag(tag: String) = apply { this.tags.add(tag) }

    /**
     * Prevents the Pokémon from moving.
     * Note: We set movement speed to 0 instead of using NoAI, so the boss
     * can still animate, look at players, and use attacks like a "Turret".
     */
    fun setImmovable(value: Boolean = true) = apply { this.immovable = value }

    /**
     * Makes the Pokémon completely immune to standard damage (hits, arrows, etc.).
     */
    fun setInvulnerable(value: Boolean = true) = apply { this.invulnerable = value }

    /**
     * Attaches a display name for a Boss Bar.
     * (Requires a Tick Manager to render and update the bar dynamically).
     */
    fun setBossBar(name: String) = apply { this.bossBarName = name }

    /** Adds a glowing outline effect to the Pokémon. */
    fun setGlowing(value: Boolean = true) = apply { this.glowing = value }

    /** Prevents the Pokémon from naturally despawning when players leave the area. */
    fun setPersistent(value: Boolean = true) = apply { this.persistent = value }

    /** Makes the Pokémon immune to knockback effects. */
    fun setKnockbackResistant(value: Boolean = true) = apply { this.knockbackResistant = value }

    /**
     * Assigns a unique ID to this boss for tracking (e.g., Scoreboards, Damage tracking).
     */
    fun setBossId(id: String) = apply { this.bossId = id }

    /**
     * Marks this Pokémon as uncatchable (requires an Event Listener to enforce).
     */
    fun setUncatchable(value: Boolean = true) = apply { this.uncatchable = value }

    /**
     * Modifies the physical size of the Pokémon (e.g., 2.0f for double size).
     */
    fun setScale(scale: Float) = apply { this.customScale = scale }

    /**
     * Overrides the Minecraft entity's maximum health for overworld boss fights.
     */
    fun setCustomMaxHealth(hp: Double) = apply { this.customMaxHealth = hp }

    /** Links this Pokémon to a custom drop table ID for external event handling. */
    fun setCustomLoot(lootId: String) = apply { this.customLootId = lootId }

    /** Sets a trigger phrase. Useful for interrupting battles to play cutscenes or quests. */
    fun setInteractTrigger(trigger: String) = apply { this.interactTrigger = trigger }

    /** Prevents this Pokémon from dropping vanilla EXP upon defeat. */
    fun setDisableExp(value: Boolean = true) = apply { this.disableExp = value }

    /** Marks this Pokémon as hostile. (Requires custom AI/Event listener to actively attack players). */
    fun setHostile(value: Boolean = true) = apply { this.hostile = value }

    /** Restricts interaction to players holding a specific permission node. */
    fun setPermission(permission: String) = apply { this.requirePermission = permission }

    /**
     * Adds a phase transition that triggers when the boss health drops to the specified ratio.
     */
    fun addPhase(threshold: Float, action: (PokemonEntity) -> Unit) = apply {
        this.phases.add(BossPhase(threshold, action))
    }

    /**
     * Finalizes the configuration and spawns the Pokémon into the world.
     *
     * @param serverLevel The world/dimension to spawn the Pokémon in.
     * @param spawnPos The exact XYZ coordinates for the spawn.
     * @return The spawned [PokemonEntity], or null if the species is invalid.
     */
    fun spawn(serverLevel: ServerLevel, spawnPos: Vec3): PokemonEntity? {
        // Resolve Species
        val species = PokemonSpecies.getByName(speciesName.lowercase()) ?: return null
        // Create Base Pokemon Data
        val pokemon = species.create(level)

        shiny?.let { pokemon.shiny = it }

        customIvs.forEach { (stat, value) -> pokemon.ivs.set(stat, value) }
        customEvs.forEach { (stat, value) -> pokemon.evs.set(stat, value) }

        // Create the Physical Entity
        val pokemonEntity = CobblemonEntities.POKEMON.create(serverLevel) ?: return null
        pokemonEntity.pokemon = pokemon
        pokemonEntity.setPos(spawnPos.x, spawnPos.y, spawnPos.z)

        // Apply Visuals and Base Tags
        if (customName != null) {
            pokemonEntity.customName = Component.literal(customName)
            pokemonEntity.isCustomNameVisible = true
        }

        tags.forEach { pokemonEntity.addTag(it) }

        // Apply Boss & Special Modifiers
        if (invulnerable) {
            pokemonEntity.isInvulnerable = true
        }

        if (glowing) {
            pokemonEntity.setGlowingTag(true)
        }

        if (persistent) {
            pokemonEntity.setPersistenceRequired()
        }

        // Apply Attribute Modifiers
        if (immovable) {
            pokemonEntity.getAttribute(Attributes.MOVEMENT_SPEED)?.baseValue = 0.0

            val scoreboard = serverLevel.scoreboard
            var team = scoreboard.getPlayerTeam("lux_immovable")
            if (team == null) {
                team = scoreboard.addPlayerTeam("lux_immovable")
                team.collisionRule = Team.CollisionRule.NEVER
            }
            scoreboard.addPlayerToTeam(pokemonEntity.stringUUID, team)
        }

        if (knockbackResistant || immovable) {
            pokemonEntity.getAttribute(Attributes.KNOCKBACK_RESISTANCE)?.baseValue = 1.0
        }

        // Handle Boss Bar setup via Tag
        if (bossBarName != null) {
            pokemonEntity.addTag("lux_is_boss")

            val bossBar = buildBossBar(bossBarName!!) {
                color(BossEvent.BossBarColor.RED)
                overlay(BossEvent.BossBarOverlay.NOTCHED_10)
                darkenScreen(true)
                playBossMusic(true)
            }

            BossBarManager.register(pokemonEntity, bossBar, radius = 50.0)
            BossHpSynchronizer.bindToBossBar(pokemonEntity)
        }

        // Apply World Boss Modifiers
        if (customScale != 1.0f) {
            pokemon.scaleModifier = customScale
            pokemonEntity.refreshDimensions()
        }

        if (bossId != null) {
            pokemonEntity.addTag("lux_boss_id:$bossId")
        }

        if (uncatchable) {
            pokemonEntity.addTag("lux_uncatchable")
            PokemonProperties.parse("uncatchable=true").apply(pokemon)
            UncatchableManager.register()
        }

        if (customMaxHealth != null) {
            val healthAttr = pokemonEntity.getAttribute(Attributes.MAX_HEALTH)
            healthAttr?.baseValue = customMaxHealth!!
            pokemonEntity.health = customMaxHealth!!.toFloat()
        }

        // Apply Advanced Hooks
        if (customLootId != null) pokemonEntity.addTag("lux_loot:$customLootId")
        if (interactTrigger != null) pokemonEntity.addTag("lux_trigger:$interactTrigger")
        if (disableExp) pokemonEntity.addTag("lux_no_exp")
        if (hostile) pokemonEntity.addTag("lux_hostile")
        if (requirePermission != null) pokemonEntity.addTag("lux_permission:$requirePermission")

        // Spawn into the World
        serverLevel.addFreshEntity(pokemonEntity)

        if (phases.isNotEmpty()) {
            BossPhaseManager.registerPhases(pokemonEntity.uuid, phases)
        }

        LuxBossHooks.triggerSpawn(BossSpawnEvent(pokemonEntity))

        return pokemonEntity
    }
}