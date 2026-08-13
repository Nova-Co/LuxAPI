package com.novaco.luxapi.commons.init

import com.novaco.luxapi.commons.command.scanning.ClasspathScanner
import java.lang.reflect.Modifier

/**
 * Classpath-based [InitializationTask] discovery — the same `java.class.path` walk
 * [com.novaco.luxapi.commons.command.scanning.CommandScanner] uses for `@Command` classes,
 * reused here for init tasks. Carries the same scope limit: works for a standalone
 * Bukkit/Spigot plugin jar, blind to Fabric/NeoForge mod jars behind their own
 * classloaders — those two platforms use their native mod-scan APIs instead
 * (`FabricInitializerRunner`, `NeoForgeInitScanner`), not this class.
 */
object ClasspathInitScanner {

    /**
     * Scans [basePackage] for concrete [InitializationTask] implementors with a no-arg
     * constructor, instantiates each, and runs them via [InitializationRunner].
     *
     * @return The number of tasks that ran without throwing.
     */
    fun scanAndRun(
        basePackage: String,
        classLoader: ClassLoader = ClasspathInitScanner::class.java.classLoader
    ): Int {
        val tasks = ClasspathScanner.findClasses(basePackage, classLoader)
            .filter { InitializationTask::class.java.isAssignableFrom(it) && !it.isInterface && !Modifier.isAbstract(it.modifiers) }
            .mapNotNull { clazz ->
                try {
                    @Suppress("UNCHECKED_CAST")
                    clazz.getDeclaredConstructor().newInstance() as InitializationTask
                } catch (_: ReflectiveOperationException) {
                    null
                }
            }

        return InitializationRunner.runAll(tasks)
    }
}
