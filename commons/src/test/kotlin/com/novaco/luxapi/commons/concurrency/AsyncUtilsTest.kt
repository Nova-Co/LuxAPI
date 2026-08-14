package com.novaco.luxapi.commons.concurrency

import com.novaco.luxapi.commons.concurrency.AsyncUtils.handleSafely
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class AsyncUtilsTest {

    @Test
    fun `test supplyAsync completes with the block result`() {
        val future = AsyncUtils.supplyAsync { 21 * 2 }

        assertEquals(42, future.get(2, TimeUnit.SECONDS))
    }

    @Test
    fun `test allOf completes once every future has completed`() {
        val first = CompletableFuture<Int>()
        val second = CompletableFuture<Int>()

        val combined = AsyncUtils.allOf(listOf(first, second))
        assertFalse(combined.isDone)

        first.complete(1)
        assertFalse(combined.isDone)

        second.complete(2)
        combined.get(2, TimeUnit.SECONDS)
        assertTrue(combined.isDone)
    }

    @Test
    fun `test await returns the result within the timeout`() {
        val future = CompletableFuture.completedFuture("ready")

        assertEquals("ready", AsyncUtils.await(future, 1000))
    }

    @Test
    fun `test await returns null on timeout instead of throwing`() {
        val future = CompletableFuture<String>()

        assertNull(AsyncUtils.await(future, 50))
    }

    @Test
    fun `test await returns null when the future completes exceptionally`() {
        val future = CompletableFuture<String>()
        future.completeExceptionally(RuntimeException("boom"))

        assertNull(AsyncUtils.await(future, 1000))
    }

    @Test
    fun `test handleSafely unwraps the real cause instead of a CompletionException wrapper`() {
        val future = CompletableFuture<String>()
        val latch = CountDownLatch(1)
        var capturedError: Throwable? = null

        future.handleSafely(onSuccess = {}, onFailure = { error ->
            capturedError = error
            latch.countDown()
        })

        future.completeExceptionally(IllegalStateException("real cause"))

        assertTrue(latch.await(2, TimeUnit.SECONDS))
        assertEquals("real cause", capturedError?.message)
        assertTrue(capturedError is IllegalStateException)
    }

    @Test
    fun `test handleSafely invokes onSuccess with the result`() {
        val future = CompletableFuture<Int>()
        val latch = CountDownLatch(1)
        var capturedResult: Int? = null

        future.handleSafely(onSuccess = { result ->
            capturedResult = result
            latch.countDown()
        })

        future.complete(99)

        assertTrue(latch.await(2, TimeUnit.SECONDS))
        assertEquals(99, capturedResult)
    }

    @Test
    fun `test newExecutor threads use the given name prefix`() {
        val executor = AsyncUtils.newExecutor("Lux-Test")
        try {
            val threadName = AsyncUtils.supplyAsync(executor) { Thread.currentThread().name }
                .get(2, TimeUnit.SECONDS)

            assertTrue(threadName.startsWith("Lux-Test-"), "Expected a Lux-Test- prefixed name, got $threadName")
        } finally {
            executor.shutdown()
        }
    }

    @Test
    fun `test supplyAsync and runAsync with an explicit executor complete normally`() {
        val executor = AsyncUtils.newExecutor("Lux-Test-Explicit")
        try {
            assertEquals(42, AsyncUtils.supplyAsync(executor) { 42 }.get(2, TimeUnit.SECONDS))
            AsyncUtils.runAsync(executor) { }.get(2, TimeUnit.SECONDS)
        } finally {
            executor.shutdown()
        }
    }
}
