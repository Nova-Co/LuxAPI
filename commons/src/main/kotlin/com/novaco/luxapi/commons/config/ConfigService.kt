package com.novaco.luxapi.commons.config

import com.novaco.luxapi.commons.config.annotation.Comment
import com.novaco.luxapi.commons.config.annotation.Config
import org.spongepowered.configurate.yaml.NodeStyle
import org.spongepowered.configurate.yaml.YamlConfigurationLoader
import java.io.File
import java.nio.file.Path

/**
 * Service responsible for loading and saving configurations using Sponge Configurate.
 * Supports comments and automatic type mapping.
 */
object ConfigService {

    /**
     * Loads a configuration class from the specified folder.
     */
    fun <T : LuxConfig> load(clazz: Class<T>, dataFolder: File): T {
        val annotation = clazz.getAnnotation(Config::class.java)
            ?: throw IllegalArgumentException("Class ${clazz.simpleName} is missing @Config annotation.")

        val file = File(dataFolder, annotation.path)
        if (!file.parentFile.exists()) file.parentFile.mkdirs()

        val loader = createLoader(file.toPath())
        val node = loader.load()

        val instance = node.get(clazz) ?: clazz.getDeclaredConstructor().newInstance()

        if (!file.exists()) {
            save(instance, file)
        }

        instance.init(file)
        return instance
    }

    /**
     * Saves the configuration instance and applies comments from @Comment annotations.
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
                node.node(field.name).comment(commentAnnotation.value)
            }
        }

        loader.save(node)
    }

    private fun createLoader(path: Path): YamlConfigurationLoader {
        return YamlConfigurationLoader.builder()
            .path(path)
            .nodeStyle(NodeStyle.BLOCK)
            .indent(2)
            .build()
    }
}