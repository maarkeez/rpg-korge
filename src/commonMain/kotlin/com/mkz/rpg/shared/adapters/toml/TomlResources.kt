package com.mkz.rpg.shared.adapters.toml

import korlibs.io.file.VfsFile

private const val TOML_EXTENSION = ".toml"

suspend inline fun <reified T> VfsFile.readTomlFiles(): Map<String, T> {
    if (!exists()) return emptyMap()
    return listNames()
        .filter { name -> name.endsWith(tomlExtension()) }
        .sorted()
        .associate { name -> name.removeSuffix(tomlExtension()) to this[name].readTomlFile<T>() }
}

suspend inline fun <reified T> VfsFile.readTomlFile(): T {
    check(exists()) { "Resource '$path' not found" }
    val toml = readString()
    return try {
        TomlService().deserialize<T>(toml)
    } catch (exception: Exception) {
        throw IllegalStateException("Resource '$path' is invalid: ${exception.message}", exception)
    }
}

inline fun <T> fromResource(
    path: String,
    conversion: () -> T,
): T =
    try {
        conversion()
    } catch (exception: Exception) {
        throw IllegalStateException("Resource '$path' is invalid: ${exception.message}", exception)
    }

inline fun <reified E : Enum<E>> enumOf(
    field: String,
    value: String,
): E =
    enumValues<E>().firstOrNull { it.name == value }
        ?: throw IllegalArgumentException("'$field' must be one of ${enumValues<E>().map { it.name }}, but was '$value'")

fun <T : Any> required(
    field: String,
    value: T?,
): T = value ?: throw IllegalArgumentException("'$field' is required")

@PublishedApi
internal fun tomlExtension(): String = TOML_EXTENSION
