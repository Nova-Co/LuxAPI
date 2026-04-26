package com.novaco.luxapi.cobblemon.fx

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.net.messages.client.effect.SpawnSnowstormEntityParticlePacket
import com.cobblemon.mod.common.net.messages.client.animation.PlayPosableAnimationPacket
import com.cobblemon.mod.common.util.cobblemonResource
import net.minecraft.core.particles.ParticleOptions
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.protocol.game.ClientboundEntityEventPacket
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerLevel
import net.minecraft.sounds.SoundEvent
import net.minecraft.sounds.SoundSource

/**
 * A centralized wrapper for triggering cinematic visual, auditory, and animation effects.
 */
object BossFxWrapper {

    /**
     * Pre-defined generic combat and interaction animations natively supported by Cobblemon.
     */
    enum class GenericAnimation(val key: String) {
        GROWL("growl"),
        STOMP("stomp"),
        TACKLE("tackle"),
        BULLDOZE("bulldoze"),
        BODY_SLAM("bodyslam"),
        CLOSE_COMBAT("closecombat"),
        SEISMIC_TOSS("seismictoss"),
        FLAME_WHEEL("flamewheel"),
        PSYCHIC("psychic"),
        SQUISH("squish")
    }

    /**
     * Pre-defined complex Cobblemon Snowstorm effects based on internal move assets.
     */
    enum class MoveParticle(val resourceName: String) {
        FLAMETHROWER_ACTOR("moves/flamethrower/flamethrower_actor"),
        FLAMETHROWER_TARGET("moves/flamethrower/flamethrower_target"),
        HYPERFANG_TARGET("moves/hyperfang/hyperfang_target"),
        SHADOWBALL_LAUNCH("moves/shadowball/shadowball_actorlaunch"),
        THUNDERBOLT_ACTOR("moves/thunderbolt/thunderbolt_actor"),
        ICEBEAM_ACTOR("moves/icebeam/icebeam_actor"),
        EXPLOSION_BOOM("moves/explosion/explosion_actorboom")
    }

    /**
     * Pre-defined Cobblemon Snowstorm effects based on internal assets.
     */
    enum class SnowstormEffect(val resourceName: String) {
        SHINY_RING("shiny_ring"),
        EVOLUTION_START("evolution_start"),
        EVOLUTION_BURST("evolution_burst"),
        CAPTURE_HISUI("capture/hisui"),
        AILMENT_BURN("generic/ailments/burn"),
        AILMENT_POISON("generic/ailments/poison"),
        AILMENT_PARALYSIS("generic/ailments/paralysis"),
        AILMENT_SLEEP("generic/ailments/sleep");
    }

    /**
     * Pre-defined complex animation events natively supported by Cobblemon/Vanilla.
     */
    enum class EntityEvent(val id: Byte) {
        DAMAGE(2),
        DEATH(3),
        HEART_PARTICLES(7),
        VILLAGER_ANGRY(13),
        VILLAGER_HAPPY(14),
        EXPLOSION(20);
    }

    /**
     * Sends a packet to nearby clients to play a specific generic animation on the Pokemon entity.
     */
    fun playAnimation(entity: PokemonEntity, animation: GenericAnimation) {
        val level = entity.level() as? ServerLevel ?: return

        // Create the packet with a Set of animations (fallback sequence) and empty MoLang expressions
        val packet = PlayPosableAnimationPacket(
            entity.id,
            setOf(animation.key),
            emptyList()
        )

        packet.sendToPlayersAround(
            entity.x,
            entity.y,
            entity.z,
            64.0,
            level.dimension()
        )
    }

    /**
     * Forces the Pokemon entity to play its native species cry.
     */
    fun playCry(entity: PokemonEntity) {
        entity.cry()
    }

    /**
     * Plays a specific sound from the Cobblemon sound registry.
     */
    fun playCobblemonSound(entity: PokemonEntity, soundKey: String, volume: Float = 1.0f, pitch: Float = 1.0f) {
        val level = entity.level() as? ServerLevel ?: return
        val soundEventId = ResourceLocation.fromNamespaceAndPath("cobblemon", soundKey)
        val soundEvent = BuiltInRegistries.SOUND_EVENT.get(soundEventId) ?: return

        level.playSound(
            null,
            entity.x,
            entity.y,
            entity.z,
            soundEvent,
            SoundSource.HOSTILE,
            volume,
            pitch
        )
    }

    /**
     * Spawns a complex Cobblemon Snowstorm (Molang) particle effect around the entity using Enums.
     */
    fun playSnowstormEffect(entity: PokemonEntity, effect: SnowstormEffect, tags: List<String> = listOf("middle")) {
        val level = entity.level() as? ServerLevel ?: return

        val packet = SpawnSnowstormEntityParticlePacket(
            cobblemonResource(effect.resourceName),
            entity.id,
            tags
        )

        packet.sendToPlayersAround(
            entity.x,
            entity.y,
            entity.z,
            64.0,
            level.dimension()
        )
    }

    /**
     * Plays a localized standard sound effect at the exact position of the entity.
     */
    fun playSound(entity: PokemonEntity, sound: SoundEvent, volume: Float = 1.0f, pitch: Float = 1.0f) {
        val level = entity.level() as? ServerLevel ?: return
        level.playSound(
            null,
            entity.x,
            entity.y,
            entity.z,
            sound,
            SoundSource.HOSTILE,
            volume,
            pitch
        )
    }

    /**
     * Spawns a customized cluster of vanilla particles surrounding the specified entity.
     */
    fun spawnParticles(
        entity: PokemonEntity,
        particle: ParticleOptions,
        count: Int = 30,
        spreadX: Double = 1.0,
        spreadY: Double = 1.0,
        spreadZ: Double = 1.0,
        speed: Double = 0.1
    ) {
        val level = entity.level() as? ServerLevel ?: return
        level.sendParticles(
            particle,
            entity.x,
            entity.y + (entity.bbHeight / 2.0),
            entity.z,
            count,
            spreadX,
            spreadY,
            spreadZ,
            speed
        )
    }

    /**
     * Spawns a complex Cobblemon Snowstorm (Molang) particle effect using Move Enums.
     */
    fun playMoveParticle(entity: PokemonEntity, particle: MoveParticle, tags: List<String> = listOf("middle")) {
        val level = entity.level() as? ServerLevel ?: return

        val packet = SpawnSnowstormEntityParticlePacket(
            cobblemonResource(particle.resourceName),
            entity.id,
            tags
        )

        packet.sendToPlayersAround(
            entity.x,
            entity.y,
            entity.z,
            64.0,
            level.dimension()
        )
    }

    /**
     * Invokes an entity-specific animation or status event for the client to render using Enums.
     */
    fun playEntityEvent(entity: PokemonEntity, event: EntityEvent) {
        val level = entity.level() as? ServerLevel ?: return
        val packet = ClientboundEntityEventPacket(entity, event.id)
        level.server.playerList.broadcast(
            null,
            entity.x,
            entity.y,
            entity.z,
            64.0,
            level.dimension(),
            packet
        )
    }
}