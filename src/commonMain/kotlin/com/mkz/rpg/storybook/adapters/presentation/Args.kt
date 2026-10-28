package com.mkz.rpg.storybook.adapters.presentation

import com.mkz.rpg.storybook.domain.Storybook.Dto.ArgDto
import korlibs.image.color.Colors
import korlibs.image.color.RGBA

/** The args a story is displayed with, by the name of their control, e.g.: `args.int("remaining")`. */
class Args(
    private val values: Map<String, ArgDto>,
) {
    fun boolean(name: String): Boolean = requireNotNull(arg(name).boolean) { "The arg '$name' is not a boolean" }

    fun int(name: String): Int = requireNotNull(arg(name).int) { "The arg '$name' is not an int" }

    /** A text, or the option of an enum or a pick control. */
    fun text(name: String): String = requireNotNull(arg(name).text) { "The arg '$name' is not a text" }

    /** The color of a color control, kept as its hex without `#`, e.g.: `FF375F`. */
    fun color(name: String): RGBA = Colors["#${text(name)}"]

    private fun arg(name: String): ArgDto = requireNotNull(values[name]) { "The story has no arg '$name'" }
}
