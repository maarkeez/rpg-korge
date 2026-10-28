package com.mkz.rpg.storybook.adapters.location

import com.mkz.rpg.storybook.domain.Storybook.Dto.LocationDto
import korlibs.io.net.URL

/**
 * What is on show, in the query of a page URL, as Storybook writes it: the story, the args changed from its own and the
 * globals changed from their defaults, each `<name>:<value>`, separated by `;`, with the values URL-encoded, e.g.:
 * `story=screen-health-bar--full&args=remaining:3;maximum:40&globals=language:es`.
 */
object LocationQuery {
    const val STORY_PARAMETER = "story"
    const val ARGS_PARAMETER = "args"
    const val GLOBALS_PARAMETER = "globals"

    private val PARAMETERS = setOf(STORY_PARAMETER, ARGS_PARAMETER, GLOBALS_PARAMETER)
    private const val PARAMETER_SEPARATOR = "&"
    private const val VALUE_SEPARATOR = ";"
    private const val NAME_SEPARATOR = ":"

    /** What the query asks for, e.g.: `?story=screen-health-bar--full`. What can not be read is left out, never a failure. */
    fun decode(query: String): LocationDto {
        val parameters = parametersOf(query).toMap()
        return LocationDto(
            storyId = parameters[STORY_PARAMETER]?.let(::decodeOrNull),
            args = parameters[ARGS_PARAMETER]?.let(::decodeValues).orEmpty(),
            globals = parameters[GLOBALS_PARAMETER]?.let(::decodeValues).orEmpty(),
        )
    }

    /** The [query] with the [location] in place of what it kept, keeping its other parameters, without the leading `?`. */
    fun encode(
        location: LocationDto,
        query: String,
    ): String {
        val otherParameters = parametersOf(query).filterNot { (name, _) -> name in PARAMETERS }.map { (name, value) -> "$name=$value" }
        val parameters =
            listOfNotNull(
                location.storyId?.let { storyId -> "$STORY_PARAMETER=${URL.encodeComponent(storyId)}" },
                location.args.takeIf { it.isNotEmpty() }?.let { args -> "$ARGS_PARAMETER=${encodeValues(args)}" },
                location.globals.takeIf { it.isNotEmpty() }?.let { globals -> "$GLOBALS_PARAMETER=${encodeValues(globals)}" },
            )
        return (parameters + otherParameters).joinToString(PARAMETER_SEPARATOR)
    }

    // The name and raw value of each parameter, in their order.
    private fun parametersOf(query: String): List<Pair<String, String>> =
        query
            .removePrefix("?")
            .split(PARAMETER_SEPARATOR)
            .filter { it.isNotEmpty() }
            .map { parameter -> parameter.substringBefore("=") to parameter.substringAfter("=", "") }

    private fun encodeValues(values: Map<String, String>): String =
        values.entries.joinToString(VALUE_SEPARATOR) { (name, value) -> "${URL.encodeComponent(name)}$NAME_SEPARATOR${URL.encodeComponent(value)}" }

    private fun decodeValues(values: String): Map<String, String> =
        values
            .split(VALUE_SEPARATOR)
            .filter { value -> value.contains(NAME_SEPARATOR) }
            .mapNotNull { value ->
                val name = decodeOrNull(value.substringBefore(NAME_SEPARATOR)) ?: return@mapNotNull null
                decodeOrNull(value.substringAfter(NAME_SEPARATOR))?.let { decoded -> name to decoded }
            }.toMap()

    // A browser may write a space as `+`. A malformed escape, e.g.: `%G1`, can not be read.
    private fun decodeOrNull(value: String): String? = runCatching { URL.decodeComponent(value, formUrlEncoded = true) }.getOrNull()
}
