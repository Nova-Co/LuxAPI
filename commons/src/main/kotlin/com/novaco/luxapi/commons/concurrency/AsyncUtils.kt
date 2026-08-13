package com.novaco.luxapi.commons.concurrency

import java.util.concurrent.CompletableFuture
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
     * Combines multiple futures into one that completes once every one of [futures] has.
     */
    fun allOf(futures: Collection<CompletableFuture<*>>): CompletableFuture<Void> {
        return CompletableFuture.allOf(*futures.toTypedArray())
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
