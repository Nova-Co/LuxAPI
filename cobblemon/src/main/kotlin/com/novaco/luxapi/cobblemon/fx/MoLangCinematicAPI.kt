package com.novaco.luxapi.cobblemon.fx

import com.bedrockk.molang.runtime.MoLangRuntime
import com.bedrockk.molang.runtime.value.MoValue
import com.cobblemon.mod.common.api.molang.MoLangFunctions.addEntityFunctions
import com.cobblemon.mod.common.api.molang.MoLangFunctions.asMostSpecificMoLangValue
import com.cobblemon.mod.common.api.molang.MoLangFunctions.setup
import com.cobblemon.mod.common.api.moves.animations.ActionEffectContext
import com.cobblemon.mod.common.api.moves.animations.ActionEffectTimeline
import com.cobblemon.mod.common.api.moves.animations.ActionEffects
import com.cobblemon.mod.common.api.moves.animations.TargetsProvider
import com.cobblemon.mod.common.api.moves.animations.UsersProvider
import com.cobblemon.mod.common.net.messages.client.effect.RunPosableMoLangPacket
import com.cobblemon.mod.common.util.asExpressionLike
import com.cobblemon.mod.common.util.resolve
import com.cobblemon.mod.common.util.withQueryValue
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.Entity
import java.util.concurrent.CompletableFuture

/**
 * Generic wrapper around Cobblemon's real [ActionEffectTimeline]/MoLang cinematic engine — the
 * same system that drives move animations in battle — usable against arbitrary entities outside
 * of a battle context (bosses, NPCs, world events). Complements [BossFxWrapper]'s pre-defined
 * enum effects with the underlying scriptable timeline/MoLang system itself.
 */
object MoLangCinematicAPI {

    /** True if a datapack-registered `action_effects` JSON timeline exists under [id]. */
    fun hasTimeline(id: ResourceLocation): Boolean = ActionEffects.actionEffects.containsKey(id)

    /**
     * Parses an ad-hoc [ActionEffectTimeline] from a JSON string using Cobblemon's own
     * `action_effects` JSON schema (same keyframe types: `animation`, `molang`, `sequence`,
     * `parallel`, `entity_particles`, `entity_sound`, `move_to_target`, etc.) — lets a timeline be
     * authored and played without registering a real datapack file.
     */
    fun parseTimeline(json: String): ActionEffectTimeline =
        ActionEffects.gson.fromJson(json, ActionEffectTimeline::class.java)

    /**
     * Plays the datapack-registered timeline at [id] (see [ActionEffects.actionEffects]) against
     * [users]/[targets]. Returns null if no timeline is registered under [id].
     */
    fun playTimeline(
        id: ResourceLocation,
        level: ServerLevel,
        users: List<Entity> = emptyList(),
        targets: List<Entity> = emptyList(),
        queries: Map<String, MoValue> = emptyMap()
    ): CompletableFuture<Unit>? {
        val timeline = ActionEffects.actionEffects[id] ?: return null
        return playTimeline(timeline, level, users, targets, queries)
    }

    /**
     * Plays a hand-built or [parseTimeline]-parsed [ActionEffectTimeline] against [users]/[targets].
     * [queries] injects extra named MoLang query values (`q.foo`) alongside the automatic `q.user`/
     * `q.target` bindings.
     */
    fun playTimeline(
        timeline: ActionEffectTimeline,
        level: ServerLevel,
        users: List<Entity> = emptyList(),
        targets: List<Entity> = emptyList(),
        queries: Map<String, MoValue> = emptyMap()
    ): CompletableFuture<Unit> {
        val context = ActionEffectContext(
            actionEffect = timeline,
            runtime = buildRuntime(users, targets, queries),
            providers = buildProviders(users, targets),
            level = level
        )
        return timeline.run(context)
    }

    /**
     * Resolves a single raw MoLang expression string (e.g. `"q.user.health / q.user.max_health"`)
     * server-side against [users]/[targets]/[queries] and returns the raw [MoValue] result — a
     * lightweight scripting hook that doesn't require a full timeline.
     */
    fun resolve(
        expression: String,
        users: List<Entity> = emptyList(),
        targets: List<Entity> = emptyList(),
        queries: Map<String, MoValue> = emptyMap()
    ): MoValue = buildRuntime(users, targets, queries).resolve(expression.asExpressionLike())

    /**
     * Sends raw MoLang expression strings to run directly against [entity]'s client-side posable
     * (animation) runtime. Unlike [playTimeline]/[resolve], this never touches server-side MoLang
     * types — it's a pure string packet, the same primitive Cobblemon's own `entity_molang`
     * keyframe uses under the hood.
     */
    fun runClientMoLang(entity: Entity, vararg expressions: String) {
        val level = entity.level() as? ServerLevel ?: return
        val packet = RunPosableMoLangPacket(entity.id, expressions.toSet())
        packet.sendToPlayersAround(entity.x, entity.y, entity.z, 64.0, level.dimension())
    }

    private fun buildRuntime(users: List<Entity>, targets: List<Entity>, queries: Map<String, MoValue>): MoLangRuntime {
        val runtime = MoLangRuntime().setup()
        users.firstOrNull()?.let { user ->
            runtime.environment.query.addEntityFunctions(user)
            runtime.withQueryValue("user", user.asMostSpecificMoLangValue())
        }
        targets.firstOrNull()?.let { target ->
            runtime.withQueryValue("target", target.asMostSpecificMoLangValue())
        }
        queries.forEach { (name, value) -> runtime.withQueryValue(name, value) }
        return runtime
    }

    private fun buildProviders(users: List<Entity>, targets: List<Entity>): MutableList<Any> {
        val providers = mutableListOf<Any>()
        if (users.isNotEmpty()) providers.add(UsersProvider(users))
        if (targets.isNotEmpty()) providers.add(TargetsProvider(targets))
        return providers
    }
}
