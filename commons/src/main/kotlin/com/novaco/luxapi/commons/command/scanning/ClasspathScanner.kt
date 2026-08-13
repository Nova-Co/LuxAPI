package com.novaco.luxapi.commons.command.scanning

import java.io.File
import java.util.jar.JarFile

/**
 * Plain `java.class.path` classpath walker (directories and jars), extracted out of
 * [CommandScanner] so other classpath-based scanners (e.g. Bukkit's init-task discovery)
 * can reuse the same directory/jar traversal without duplicating it.
 *
 * **Scope note carried over from [CommandScanner]:** only sees a flat classpath, so this
 * works correctly for a standalone Bukkit/Spigot plugin jar but is blind to Fabric/NeoForge
 * mod jars behind their own Knot/ModLauncher classloaders.
 */
object ClasspathScanner {

    /**
     * Finds every class under [basePackage] (and its subpackages) on the classpath,
     * loading each via [classLoader]. Classes that fail to load are silently skipped.
     */
    fun findClasses(basePackage: String, classLoader: ClassLoader): List<Class<*>> {
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
