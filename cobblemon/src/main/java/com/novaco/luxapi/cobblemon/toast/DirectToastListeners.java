package com.novaco.luxapi.cobblemon.toast;

import com.cobblemon.mod.common.api.toast.Toast;
import net.minecraft.server.level.ServerPlayer;

/**
 * Forwards an already-built {@code ServerPlayer[]} to {@link Toast#addListeners} directly,
 * same reasoning as {@link com.novaco.luxapi.commons.reflection.DirectInvoker}: Java passes an
 * existing array straight through to a vararg parameter with no copy, while Kotlin's spread
 * operator always inserts one. {@link Toast#addListeners} has no {@code Collection} overload
 * (checked via javap on Cobblemon's own compiled class), so the array itself is still required —
 * this only removes the second, Kotlin-side copy on top of it.
 */
public final class DirectToastListeners {

    private DirectToastListeners() {
    }

    public static void addListeners(Toast toast, ServerPlayer[] players) {
        toast.addListeners(players);
    }
}
