package com.novaco.luxapi.commons.config

import com.novaco.luxapi.commons.config.annotation.Comment
import com.novaco.luxapi.commons.config.annotation.Config
import org.spongepowered.configurate.yaml.NodeStyle
import org.spongepowered.configurate.yaml.YamlConfigurationLoader
import java.io.File
import java.nio.file.Path

/**
 * A centralized service for managing the persistence of LuxConfig objects.
 * Utilizes Sponge Configurate for high-performance YAML processing and
 * supports automatic comment injection via annotations.
 */
object ConfigService {

    /**
     * Loads a configuration instance from the filesystem.
     * If the file does not exist, it will be created with default values.
     *
     * @param clazz The class type extending LuxConfig to be loaded.
     * @param dataFolder The root directory where the config file should reside.
     * @return A fully populated instance of the specified configuration class.
     * @throws IllegalArgumentException If the class is missing the @Config annotation.
     */
    fun <T : LuxConfig> load(clazz: Class<T>, dataFolder: File): T {
        val annotation = clazz.getAnnotation(Config::class.java)
            ?: throw IllegalArgumentException("Class ${clazz.simpleName} must be annotated with @Config.")

        val file = File(dataFolder, annotation.path)
        if (!file.parentFile.exists()) file.parentFile.mkdirs()

        val loader = createLoader(file.toPath())
        val node = loader.load()
        val instance = node.get(clazz) ?: clazz.getDeclaredConstructor().newInstance()

        instance.init(file)
        save(instance, file)
        return instance
    }

    /**
     * Persists the provided configuration instance to a YAML file.
     * Automatically applies class-level and field-level comments.
     *
     * @param instance The configuration object to save.
     * @param file The destination file on the disk.
     */
    fun save(instance: Any, file: File) {
        val loader = createLoader(file.toPath())
        val node = loader.createNode()

        node.set(instance::class.java, instance)

        val classComment = instance::class.java.getAnnotation(Comment::class.java)
        if (classComment != null) {
            node.comment(classComment.value)
        }

        instance::class.java.declaredFields.forEach { field ->
            val commentAnnotation = field.getAnnotation(Comment::class.java)
            if (commentAnnotation != null) {
                field.isAccessible = true
                node.node(field.name).comment(commentAnnotation.value)
            }
        }

        loader.save(node)
    }

    /**
     * Configures and builds a YAML loader with standardized settings.
     *
     * @param path The NIO Path to the target file.
     * @return A configured YamlConfigurationLoader.
     */
    private fun createLoader(path: Path): YamlConfigurationLoader {
        return YamlConfigurationLoader.builder()
            .path(path)
            .nodeStyle(NodeStyle.BLOCK)
            .indent(2)
            .defaultOptions { options ->
                options.shouldCopyDefaults(true)
            }
            .build()
    }
}