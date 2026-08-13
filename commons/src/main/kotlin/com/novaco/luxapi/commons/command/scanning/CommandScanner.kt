package com.novaco.luxapi.commons.command.scanning

import com.novaco.luxapi.commons.command.CommandManager
import com.novaco.luxapi.commons.command.annotation.Command
import java.io.File
import java.lang.reflect.Modifier
import java.util.jar.JarFile

/**
 * Classpath-scanning auto-registration for `@Command`-annotated classes, so devs
 * don't have to hand-call `manager.register(...)` per command class.
 *
 * **Scope note:** resolves classes via `java.class.path`, which only enumerates the
 * flat classpath a plain JVM process was launched with — this covers a standalone
 * Bukkit/Spigot plugin jar correctly. Fabric/NeoForge load mod jars through their own
 * isolated (Knot/ModLauncher) classloaders, which are not reflected in `java.class.path`,
 * so scanning silently finds nothing there. On those platforms, discover the command
 * classes yourself (e.g. via a small static list) and pass them to [registerAll] instead —
 * annotation classes still only need a no-arg constructor either way.
 */
object CommandScanner {

    /**
     * Scans [basePackage] (and its subpackages) on the classpath for `@Command`-annotated,
     * concrete classes exposing a no-arg constructor, instantiates each, and registers it
     * with [manager]. Classes that can't be instantiated this way are skipped, not thrown.
     *
     * @param manager The [CommandManager] to register discovered commands into.
     * @param basePackage The root package to scan, e.g. `"com.example.myplugin.commands"`.
     * @param classLoader The classloader used to load discovered classes.
     * @return The number of command classes successfully registered.
     */
    fun scanAndRegister(
        manager: CommandManager,
        basePackage: String,
        classLoader: ClassLoader = CommandScanner::class.java.classLoader
    ): Int {
        var registered = 0
        for (clazz in findClasses(basePackage, classLoader)) {
            if (!clazz.isAnnotationPresent(Command::class.java)) continue
            if (clazz.isInterface || Modifier.isAbstract(clazz.modifiers)) continue

            try {
                val instance = clazz.getDeclaredConstructor().newInstance()
                manager.register(instance)
                registered++
            } catch (_: ReflectiveOperationException) {
                // No no-arg constructor, or construction/registration failed — skip silently,
                // matching a scan's "best-effort discovery" contract.
            }
        }
        return registered
    }

    /**
     * Registers an explicit set of already-instantiated command objects.
     * The manual-fallback counterpart to [scanAndRegister] for platforms
     * (Fabric/NeoForge) where classpath scanning can't see mod jars.
     */
    fun registerAll(manager: CommandManager, instances: Collection<Any>) {
        instances.forEach { manager.register(it) }
    }

    private fun findClasses(basePackage: String, classLoader: ClassLoader): List<Class<*>> {
        val path = basePackage.replace('.', '/')
        val classpathEntries = System.getProperty("java.class.path", "")
            .split(File.pathSeparatorChar)
            .filter { it.isNotBlank() }

        val classNames = mutableListOf<String>()

        for (entry in classpathEntries) {
            val file = File(entry)
            when {
                file.isDirectory -> {
                    val root = File(file, path)
                    if (root.isDirectory) {
                        root.walkTopDown()
                            .filter { it.isFile && it.name.endsWith(".class") }
                            .forEach { classFile ->
                                val relative = classFile.relativeTo(file).path
                                    .removeSuffix(".class")
                                    .replace(File.separatorChar, '.')
                                classNames.add(relative)
                            }
                    }
                }
                file.isFile && file.name.endsWith(".jar") -> {
                    runCatching {
                        JarFile(file).use { jar ->
                            jar.entries().asSequence()
                                .filter { !it.isDirectory && it.name.startsWith("$path/") && it.name.endsWith(".class") }
                                .forEach { entryFile ->
                                    val className = entryFile.name.removeSuffix(".class").replace('/', '.')
                                    classNames.add(className)
                                }
                        }
                    }
                }
            }
        }

        return classNames.mapNotNull { className ->
            try {
                Class.forName(className, false, classLoader)
            } catch (_: Throwable) {
                null
            }
        }
    }
}
