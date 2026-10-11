package com.mkz.rpg.shared.adapters.toml

import korlibs.io.file.VfsFile

private const val TOML_EXTENSION = ".toml"

/**
 * Reads every `<id>.toml` file of this directory, keyed by id and sorted by id.
 * Returns no definitions when the directory does not exist.
 */
suspend inline fun <reified T> VfsFile.readTomlFiles(): Map<String, T> {
    if (!exists()) return emptyMap()
    return listNames()
        .filter { name -> name.endsWith(tomlExtension()) }
        .sorted()
        .associate { name -> name.removeSuffix(tomlExtension()) to this[name].readTomlFile<T>() }
}

/** Reads this TOML file, failing with the file path when it is missing or invalid. */
suspend inline fun <reified T> VfsFile.readTomlFile(): T {
    check(exists()) { "Resource '$path' not found" }
    val toml = readString()
    return try {
        TomlService().deserialize<T>(toml)
    } catch (exception: Exception) {
        throw IllegalStateException("Resource '$path' is invalid: ${exception.message}", exception)
    }
}

/** Converts a definition read from the resource at [path], failing with the [path] when the definition is not valid. */
inline fun <T> fromResource(
    path: String,
    conversion: () -> T,
): T =
    try {
        conversion()
    } catch (exception: Exception) {
        throw IllegalStateException("Resource '$path' is invalid: ${exception.message}", exception)
    }

/** Parses an enum [value] by its name, e.g.: `"DECREASE_HEALTH"`. */
inline fun <reified E : Enum<E>> enumOf(
    field: String,
    value: String,
): E =
    enumValues<E>().firstOrNull { it.name == value }
        ?: throw IllegalArgumentException("'$field' must be one of ${enumValues<E>().map { it.name }}, but was '$value'")

/** Requires a [value] that is only optional for some types, e.g.: `damage` is required by `DECREASE_HEALTH` outcomes. */
fun <T : Any> required(
    field: String,
    value: T?,
): T = value ?: throw IllegalArgumentException("'$field' is required")

@PublishedApi
internal fun tomlExtension(): String = TOML_EXTENSION
