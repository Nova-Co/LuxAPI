package com.novaco.luxapi.neoforge.init

import com.novaco.luxapi.commons.init.InitializationRunner
import com.novaco.luxapi.commons.init.InitializationTask
import net.neoforged.fml.ModList
import org.objectweb.asm.Type
import org.slf4j.LoggerFactory

/**
 * Discovers and runs [InitializationTask]s via NeoForge's own built-in mod-file scan
 * data — the platform-native discovery backend for NeoForge, mirroring how a comparable
 * dev API's Forge target scans `ModList.getAllScanData()` rather than walking the
 * classpath. This sees every loaded mod's classes (including LuxAPI-consuming mods
 * shaded into their own jar), not just LuxAPI's own.
 */
object NeoForgeInitScanner {

    private val logger = LoggerFactory.getLogger("luxapi-init")
    private val TASK_TYPE: Type = Type.getType(InitializationTask::class.java)

    /**
     * Scans every loaded mod's indexed classes for concrete [InitializationTask]
     * implementors, instantiates each via its no-arg constructor, and runs them.
     *
     * @return The number of tasks that ran without throwing.
     */
    fun scanAndRun(): Int {
        val tasks = ModList.get().allScanData
            .flatMap { it.classes }
            .filter { it.interfaces.contains(TASK_TYPE) }
            .mapNotNull { classData ->
                try {
                    val clazz = Class.forName(classData.clazz.className)
                    @Suppress("UNCHECKED_CAST")
                    clazz.getDeclaredConstructor().newInstance() as InitializationTask
                } catch (e: Exception) {
                    logger.error("Failed to load LuxAPI init task class ${classData.clazz.className}", e)
                    null
                }
            }

        return InitializationRunner.runAll(tasks)
    }
}
