package com.novaco.luxapi.commons.concurrency;

import java.util.concurrent.CompletableFuture;

/**
 * Forwards an already-built {@code CompletableFuture<?>[]} to {@link CompletableFuture#allOf}
 * directly, same reasoning as {@link com.novaco.luxapi.commons.reflection.DirectInvoker}: Java
 * passes an existing array straight through to a vararg parameter with no copy, while Kotlin's
 * spread operator always inserts one. Same {@code allOf} semantics — this changes nothing about
 * completion/exception behavior, only how the array reaches the call.
 */
public final class DirectAllOf {

    private DirectAllOf() {
    }

    public static CompletableFuture<Void> allOf(CompletableFuture<?>[] futures) {
        return CompletableFuture.allOf(futures);
    }
}
