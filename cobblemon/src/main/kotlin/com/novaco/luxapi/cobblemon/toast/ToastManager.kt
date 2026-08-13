package com.novaco.luxapi.cobblemon.toast

import com.cobblemon.mod.common.api.toast.Toast
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.item.ItemStack

/**
 * Cobblemon's own [Toast] is already a complete, self-syncing, publicly constructible
 * server-side API (construct it, [Toast.addListeners], mutate `title`/`description`/
 * `progress` for live updates, [Toast.expire] when done) — it is not a client-only
 * system, and wrapping its full surface here would just be dead, compiler-shadowed
 * passthrough (same reasoning documented on
 * [com.novaco.luxapi.cobblemon.npc.extensions.NPCFxExtensions] for skipping
 * same-signature wrappers).
 *
 * This object adds exactly one thing Cobblemon doesn't already offer: a one-call
 * convenience for the common "fire and forget" case, where the caller doesn't need
 * the returned [Toast] to mutate it later. For anything stateful (progress bars,
 * live-updating text), construct a [Toast] directly instead.
 */
object ToastManager {

    /**
     * Constructs a [Toast] from [title]/[description]/[icon], shows it to every
     * player in [players], and — if [expireAfterSeconds] is given — schedules its
     * automatic [Toast.expire] via Cobblemon's own [Toast.expireAfter]. Returns the
     * [Toast] in case the caller wants to mutate or expire it manually instead.
     */
    fun sendToast(
        players: Collection<ServerPlayer>,
        title: Component,
        description: Component,
        icon: ItemStack = ItemStack.EMPTY,
        expireAfterSeconds: Float? = null
    ): Toast {
        val toast = Toast(title, description, icon)
        toast.addListeners(*players.toTypedArray())
        expireAfterSeconds?.let { toast.expireAfter(it) }
        return toast
    }
}
