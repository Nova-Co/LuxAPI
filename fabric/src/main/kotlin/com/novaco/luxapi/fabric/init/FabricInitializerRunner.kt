package com.novaco.luxapi.fabric.init

import com.novaco.luxapi.commons.init.InitializationTask
import net.fabricmc.loader.api.FabricLoader

/**
 * Discovers and runs [InitializationTask]s via Fabric Loader's own custom-entrypoint
 * mechanism — the platform-native discovery backend for Fabric, as opposed to
 * `commons`' classpath-walking `ClasspathInitScanner` (which can't see mod jars behind
 * Fabric's Knot classloader anyway).
 *
 * Any mod — including a consumer of LuxAPI, not just LuxAPI itself — opts in by adding
 * to its own `fabric.mod.json`:
 * ```json
 * "entrypoints": {
 *   "luxapi:init": ["com.example.mymod.MyInitTask"]
 * }
 * ```
 * No registration call is needed beyond that declaration; Fabric Loader indexes
 * entrypoints from every loaded mod's manifest at launch.
 */
object FabricInitializerRunner {

    private const val ENTRYPOINT_KEY = "luxapi:init"

    /**
     * Runs every [InitializationTask] any loaded mod declared under the `luxapi:init`
     * entrypoint key. A task from one mod throwing doesn't stop the others from running.
     *
     * @return The number of tasks that ran without throwing.
     */
    fun runAll(): Int {
        var succeeded = 0
        for (container in FabricLoader.getInstance().getEntrypointContainers(ENTRYPOINT_KEY, InitializationTask::class.java)) {
            try {
                container.entrypoint.run()
                succeeded++
            } catch (e: Exception) {
                val modId = container.provider.metadata.id
                LuxFabricInitLogger.logger.error("LuxAPI init task declared by mod '$modId' failed", e)
            }
        }
        return succeeded
    }
}

/**
 * Standalone logger holder so [FabricInitializerRunner] doesn't need to depend on
 * `LuxFabricInitializer`'s companion object just to log a failure.
 */
private object LuxFabricInitLogger {
    val logger: org.slf4j.Logger = org.slf4j.LoggerFactory.getLogger("luxapi-init")
}
