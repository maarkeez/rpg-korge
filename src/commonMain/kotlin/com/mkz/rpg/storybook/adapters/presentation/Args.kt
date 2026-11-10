package com.mkz.rpg.storybook.adapters.presentation

import com.mkz.rpg.storybook.domain.Storybook.Dto.ValueDto
import korlibs.image.color.Colors
import korlibs.image.color.RGBA

class Args(
    private val values: Map<String, ValueDto>,
) {
    fun boolean(name: String): Boolean = requireNotNull(arg(name).boolean) { "The arg '$name' is not a boolean" }

    fun int(name: String): Int = requireNotNull(arg(name).int) { "The arg '$name' is not an int" }

    fun text(name: String): String = requireNotNull(arg(name).text) { "The arg '$name' is not a text" }

    fun color(name: String): RGBA = Colors["#${text(name)}"]

    private fun arg(name: String): ValueDto = requireNotNull(values[name]) { "The story has no arg '$name'" }
}
