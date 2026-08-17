package com.novaco.luxapi.commons.init

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class InitializationRunnerTest {

    @Test
    fun `test runAll runs every task and counts successes`() {
        var ran = 0
        val tasks = listOf(
            InitializationTask { ran++ },
            InitializationTask { ran++ }
        )

        val succeeded = InitializationRunner.runAll(tasks)

        assertEquals(2, ran)
        assertEquals(2, succeeded)
    }

    @Test
    fun `test one task throwing does not stop the others from running`() {
        var ranAfterFailure = false
        val tasks = listOf(
            InitializationTask { throw IllegalStateException("boom") },
            InitializationTask { ranAfterFailure = true }
        )

        val succeeded = InitializationRunner.runAll(tasks)

        assertTrue(ranAfterFailure)
        assertEquals(1, succeeded)
    }
}
