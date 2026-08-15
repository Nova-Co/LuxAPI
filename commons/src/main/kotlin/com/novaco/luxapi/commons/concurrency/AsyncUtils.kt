package com.novaco.luxapi.commons.concurrency

import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

/**
 * Thin [CompletableFuture] helpers for the common async/future patterns that were
 * previously copy-pasted per call site (e.g. `FabricPlayerManager`'s coroutine-based
 * delayed eviction) — a plain Java-interop future, not a platform- or coroutine-specific
 * mechanism, so it works identically across `fabric`/`neoforge`/`bukkit`.
 */
object AsyncUtils {

    /**
     * Runs [block] on the common ForkJoinPool and completes the returned future with
     * its result. If [block] throws, the future completes exceptionally.
     */
    fun <T> supplyAsync(block: () -> T): CompletableFuture<T> = CompletableFuture.supplyAsync(block)

    /**
     * Runs [block] on the common ForkJoinPool for its side effects only.
     */
    fun runAsync(block: () -> Unit): CompletableFuture<Void> = CompletableFuture.runAsync(block)

    /**
     * Runs [block] on [executor] instead of the common ForkJoinPool and completes the
     * returned future with its result. Use this (with [newExecutor]) for work that
     * shouldn't share the JVM-wide common pool with everything else on the server
     * (e.g. calls to a third-party HTTP endpoint that might stall).
     */
    fun <T> supplyAsync(executor: ExecutorService, block: () -> T): CompletableFuture<T> =
        CompletableFuture.supplyAsync(block, executor)

    /**
     * Runs [block] on [executor] instead of the common ForkJoinPool, for its side effects only.
     */
    fun runAsync(executor: ExecutorService, block: () -> Unit): CompletableFuture<Void> =
        CompletableFuture.runAsync(block, executor)

    /**
     * Creates a fixed-size [ExecutorService] whose threads are named `"$name-N"` (via
     * [NamedThreadFactory]) instead of the JVM's generic pool-thread names, and run as
     * daemon threads so they never block server shutdown. Intended to be created once
     * per subsystem and reused, not created per call.
     *
     * @param name The thread-name prefix and effective identity of this executor.
     * @param poolSize The number of worker threads to run concurrently. Defaults to 1,
     *   which is enough for low-volume background work (webhooks, notifications) and
     *   keeps that work fully isolated from the common pool without over-provisioning.
     */
    fun newExecutor(name: String, poolSize: Int = 1): ExecutorService =
        Executors.newFixedThreadPool(poolSize, NamedThreadFactory(name))

    /**
     * Combines multiple futures into one that completes once every one of [futures] has.
     */
    fun allOf(futures: Collection<CompletableFuture<*>>): CompletableFuture<Void> {
        return DirectAllOf.allOf(futures.toTypedArray())
    }

    /**
     * Blocks the calling thread for this future's result, up to [timeoutMillis].
     * Returns null instead of throwing on timeout, cancellation, or an exceptional result —
     * intended for call sites that already treat "no result" and "failed" the same way.
     */
    fun <T> await(future: CompletableFuture<T>, timeoutMillis: Long): T? {
        return try {
            future.get(timeoutMillis, TimeUnit.MILLISECONDS)
        } catch (_: TimeoutException) {
            null
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Attaches success/failure callbacks to this future without needing to distinguish
     * `CompletionException` wrapping from the original cause — [onFailure] always
     * receives the real underlying [Throwable].
     */
    fun <T> CompletableFuture<T>.handleSafely(onSuccess: (T) -> Unit, onFailure: (Throwable) -> Unit = {}) {
        this.whenComplete { result, throwable ->
            if (throwable != null) {
                onFailure(throwable.cause ?: throwable)
            } else {
                onSuccess(result)
            }
        }
    }
}
