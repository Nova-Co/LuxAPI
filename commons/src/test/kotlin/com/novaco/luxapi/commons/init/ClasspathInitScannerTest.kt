package com.novaco.luxapi.commons.init

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class ScannableTestTask : InitializationTask {
    override fun run() {
        ScannableTestTask.ranCount++
    }

    companion object {
        var ranCount = 0
    }
}

abstract class UnscannableAbstractTask : InitializationTask

class ClasspathInitScannerTest {

    @Test
    fun `test scanAndRun discovers and runs annotated tasks on the test classpath`() {
        ScannableTestTask.ranCount = 0

        val succeeded = ClasspathInitScanner.scanAndRun("com.novaco.luxapi.commons.init")

        assertTrue(succeeded >= 1, "Expected at least ScannableTestTask to be discovered and run")
        assertTrue(ScannableTestTask.ranCount >= 1)
    }

    @Test
    fun `test scanAndRun skips abstract classes without instantiating them`() {
        // Sanity check that scanning a package containing an abstract InitializationTask
        // doesn't throw — it should be silently filtered out, not attempted.
        assertDoesNotThrow {
            ClasspathInitScanner.scanAndRun("com.novaco.luxapi.commons.init")
        }
    }
}
