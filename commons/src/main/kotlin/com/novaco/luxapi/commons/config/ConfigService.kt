package com.novaco.luxapi.commons.config

import com.novaco.luxapi.commons.config.annotation.Comment
import com.novaco.luxapi.commons.config.annotation.Config
import com.novaco.luxapi.commons.config.annotation.ScalarSerializers
import com.novaco.luxapi.commons.config.exception.ConfigException
import com.novaco.luxapi.commons.config.serializer.ConfigTypeSerializerRegistry
import org.spongepowered.configurate.ConfigurateException
import org.spongepowered.configurate.serialize.TypeSerializer
import org.spongepowered.configurate.serialize.TypeSerializerCollection
import org.spongepowered.configurate.util.NamingSchemes
import org.spongepowered.configurate.yaml.NodeStyle
import org.spongepowered.configurate.yaml.YamlConfigurationLoader
import java.io.File
import java.lang.reflect.ParameterizedType
import java.nio.file.Path

/**
 * A centralized service for managing the persistence of LuxConfig objects.
 * Utilizes Sponge Configurate for high-performance YAML processing and
 * supports automatic comment injection via annotations.
 */
object ConfigService {

    /**
     * Loads a configuration instance from the filesystem.
     * If the file does not exist, it will be created with default values
     * (or copied from the class's `@Config(resource = ...)` if set).
     *
     * @param clazz The class type extending LuxConfig to be loaded.
     * @param dataFolder The root directory where the config file should reside.
     * @return A fully populated instance of the specified configuration class.
     * @throws IllegalArgumentException If the class is missing the @Config annotation.
     * @throws ConfigException If the file can't be read, parsed, or its default resource copied.
     */
    fun <T : LuxConfig> load(clazz: Class<T>, dataFolder: File): T {
        val annotation = requireConfigAnnotation(clazz)
        val file = File(dataFolder, annotation.path)
        return load(clazz, file, annotation.resource)
    }

    /**
     * Loads every `*.yml` file directly inside [directory] as an instance of [clazz],
     * e.g. a folder of per-arena or per-world configs sharing one shape.
     * The `@Config` annotation's `path` is ignored here (each file supplies its own path);
     * `resource` is still honored per class but has no per-file default to copy from.
     *
     * @throws IllegalArgumentException If the class is missing the @Config annotation.
     * @throws ConfigException If any file fails to load or parse.
     */
    fun <T : LuxConfig> loadAll(clazz: Class<T>, directory: File): List<T> {
        requireConfigAnnotation(clazz)
        if (!directory.exists()) directory.mkdirs()

        return directory.listFiles { file -> file.isFile && file.extension.equals("yml", ignoreCase = true) }
            ?.sortedBy { it.name }
            ?.map { load(clazz, it, "") }
            ?: emptyList()
    }

    /**
     * Persists the provided configuration instance to a YAML file.
     * Automatically applies class-level and field-level comments.
     *
     * @param instance The configuration object to save.
     * @param file The destination file on the disk.
     * @throws ConfigException If the file can't be written.
     */
    fun save(instance: Any, file: File) {
        try {
            val classComment = instance::class.java.getAnnotation(Comment::class.java)?.value
            val fieldComments = collectFieldComments(instance)

            val loader = createLoader(file.toPath(), instance::class.java)
            val options = if (classComment != null) loader.defaultOptions().header(classComment) else loader.defaultOptions()
            val node = loader.createNode(options)

            node.set(instance::class.java, instance)
            loader.save(node)

            if (fieldComments.isNotEmpty()) {
                insertFieldComments(file, fieldComments)
            }
        } catch (e: ConfigurateException) {
            throw ConfigException("Failed to save config to ${file.path}", e)
        }
    }

    /**
     * Field-level `@Comment`s keyed by the same [NamingSchemes.LOWER_CASE_DASHED] name
     * Configurate's object mapper serializes that field under. Only [instance]'s own declared
     * fields — a nested config type's fields were never covered by `@Comment` either.
     */
    private fun collectFieldComments(instance: Any): Map<String, String> {
        return instance::class.java.declaredFields.mapNotNull { field ->
            field.getAnnotation(Comment::class.java)?.let { annotation ->
                NamingSchemes.LOWER_CASE_DASHED.coerce(field.name) to annotation.value
            }
        }.toMap()
    }

    /**
     * Configurate 4.1.2's YAML writer has no per-node comment emission — only the single
     * document-level header, which [save] already uses via [org.spongepowered.configurate.ConfigurationOptions.header]
     * for the class-level `@Comment`. A field-level `@Comment` has nothing built-in to attach
     * to, so it's written by patching the file Configurate already saved: a `# ...` line
     * inserted directly above the matching top-level `key:` line. Deliberately top-level only
     * — see [collectFieldComments].
     */
    private fun insertFieldComments(file: File, fieldComments: Map<String, String>) {
        val topLevelKey = Regex("""^([\w-]+):.*$""")
        val output = StringBuilder()

        file.readLines().forEach { line ->
            val key = topLevelKey.find(line)?.groupValues?.get(1)
            if (key != null) {
                fieldComments[key]?.lines()?.forEach { commentLine -> output.append("# ").append(commentLine).append('\n') }
            }
            output.append(line).append('\n')
        }

        file.writeText(output.toString())
    }

    private fun <T : LuxConfig> load(clazz: Class<T>, file: File, resource: String): T {
        if (!file.parentFile.exists()) file.parentFile.mkdirs()
        if (!file.exists() && resource.isNotBlank()) {
            copyDefaultResource(clazz, resource, file)
        }

        try {
            val loader = createLoader(file.toPath(), clazz)
            val node = loader.load()
            val instance = node.get(clazz) ?: clazz.getDeclaredConstructor().newInstance()

            instance.init(file)
            save(instance, file)
            return instance
        } catch (e: ConfigurateException) {
            throw ConfigException("Failed to load config from ${file.path}", e)
        }
    }

    private fun requireConfigAnnotation(clazz: Class<*>): Config {
        return clazz.getAnnotation(Config::class.java)
            ?: throw IllegalArgumentException("Class ${clazz.simpleName} must be annotated with @Config.")
    }

    private fun copyDefaultResource(clazz: Class<*>, resource: String, target: File) {
        val input = clazz.getResourceAsStream("/$resource")
            ?: throw ConfigException("Default resource '$resource' not found on classpath for ${clazz.simpleName}")

        input.use { stream ->
            target.outputStream().use { output -> stream.copyTo(output) }
        }
    }

    /**
     * Configures and builds a YAML loader with standardized settings, folding in any
     * serializers registered via [ConfigTypeSerializerRegistry] on top of Configurate's
     * defaults, plus any [ScalarSerializers] declared on [clazz] itself.
     *
     * @param path The NIO Path to the target file.
     * @param clazz The config class being loaded/saved, if known, for its `@ScalarSerializers`.
     * @return A configured YamlConfigurationLoader.
     */
    private fun createLoader(path: Path, clazz: Class<*>? = null): YamlConfigurationLoader {
        return YamlConfigurationLoader.builder()
            .path(path)
            .nodeStyle(NodeStyle.BLOCK)
            .indent(2)
            .defaultOptions { options ->
                options.shouldCopyDefaults(true)
                    .serializers { builder ->
                        ConfigTypeSerializerRegistry.getAll().forEach { (type, serializer) ->
                            registerErased<Any>(builder, type, serializer)
                        }
                        resolveScalarSerializers(clazz).forEach { (type, serializer) ->
                            registerErased<Any>(builder, type, serializer)
                        }
                    }
            }
            .build()
    }

    /**
     * Uses `registerExact` (not `register`) deliberately: `register` matches by assignability,
     * so a serializer registered under a base interface (e.g. a [com.novaco.luxapi.commons.config.serializer.PolymorphicConfigRegistry]
     * entry) would also match every concrete implementation. That's fatal for a polymorphic
     * serializer specifically, since its own `serialize()` calls `node.set(concreteClass, obj)`
     * to fall through to the default ObjectMapper serialization — with assignable matching, that
     * call re-matches the same serializer instead, recursing until a StackOverflowError.
     */
    @Suppress("UNCHECKED_CAST")
    private fun <T> registerErased(builder: TypeSerializerCollection.Builder, type: Class<*>, serializer: TypeSerializer<*>) {
        builder.registerExact(type as Class<T>, serializer as TypeSerializer<T>)
    }

    private fun resolveScalarSerializers(clazz: Class<*>?): List<Pair<Class<*>, TypeSerializer<*>>> {
        val annotation = clazz?.getAnnotation(ScalarSerializers::class.java) ?: return emptyList()
        return annotation.value.map { kClass ->
            val serializerClass = kClass.java
            resolveTargetType(serializerClass) to instantiateSerializer(serializerClass)
        }
    }

    /**
     * A [ScalarSerializers] entry must directly implement `TypeSerializer<T>` with a concrete
     * type argument — this doesn't walk further up the hierarchy to find it.
     */
    private fun resolveTargetType(serializerClass: Class<out TypeSerializer<*>>): Class<*> {
        val parameterized = serializerClass.genericInterfaces
            .filterIsInstance<ParameterizedType>()
            .firstOrNull { it.rawType == TypeSerializer::class.java }
            ?: throw ConfigException("${serializerClass.simpleName} must directly implement TypeSerializer<T> with a concrete type argument to be used with @ScalarSerializers")

        return parameterized.actualTypeArguments[0] as? Class<*>
            ?: throw ConfigException("${serializerClass.simpleName}'s TypeSerializer type argument must be a concrete class")
    }

    /** Supports both a Kotlin `object` singleton (via its JVM `INSTANCE` field) and a plain no-arg-constructor class. */
    private fun instantiateSerializer(serializerClass: Class<out TypeSerializer<*>>): TypeSerializer<*> {
        return runCatching { serializerClass.getField("INSTANCE").get(null) as TypeSerializer<*> }
            .getOrElse { serializerClass.getDeclaredConstructor().newInstance() }
    }
}
