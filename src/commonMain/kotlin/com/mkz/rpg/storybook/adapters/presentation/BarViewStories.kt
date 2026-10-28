package com.mkz.rpg.storybook.adapters.presentation

import com.mkz.rpg.shared.adapters.presentation.BarView
import com.mkz.rpg.storybook.domain.Storybook.Dto.ArgDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.ComponentDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.ControlDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.StoryDto
import korlibs.korge.view.View
import korlibs.math.geom.Size

/** The stories of [BarView], at the size the battle unit info displays its bars, in any color. */
object BarViewStories : StoryBinding {
    private val SIZE = Size(281.5, 16)
    private const val MAXIMUM_VALUE = 30
    private const val VALUE_LIMIT = 999

    private const val REMAINING = "remaining"
    private const val MAXIMUM = "maximum"
    private const val COLOR = "color"

    private const val HEALTH_COLOR = "FF375F"
    private const val MANA_COLOR = "0091FF"

    private val full = StoryDto(id = "shared-bar--full", name = "Full", description = "A bar filled up to its maximum.", args = emptyMap())
    private val half = StoryDto(id = "shared-bar--half", name = "Half", description = "A bar filled up to half of its maximum.", args = mapOf(REMAINING to ArgDto.int(MAXIMUM_VALUE / 2)))
    private val empty = StoryDto(id = "shared-bar--empty", name = "Empty", description = "A bar with nothing left, over its grey background.", args = mapOf(REMAINING to ArgDto.int(0)))
    private val otherColor =
        StoryDto(
            id = "shared-bar--other-color",
            name = "Other color",
            description = "The color of the mana bar instead of the health bar's: the bar fills up with any color.",
            args =
                mapOf(
                    COLOR to ArgDto.text(MANA_COLOR),
                ),
        )

    override fun component(context: StoryContext) =
        ComponentDto(
            id = "shared-bar",
            group = "Shared",
            title = "Bar",
            description = "The base of the health and mana bars: the remaining value out of its maximum, filled with a color, and written over it.",
            controls = listOf(ControlDto.int(REMAINING, minimum = 0, maximum = VALUE_LIMIT), ControlDto.int(MAXIMUM, minimum = 1, maximum = VALUE_LIMIT), ControlDto.color(COLOR)),
            args = mapOf(REMAINING to ArgDto.int(MAXIMUM_VALUE), MAXIMUM to ArgDto.int(MAXIMUM_VALUE), COLOR to ArgDto.text(HEALTH_COLOR)),
            stories = listOf(full, half, empty, otherColor),
        )

    override fun render(
        args: Args,
        context: StoryContext,
    ): View = BarView(SIZE, args.color(COLOR)).apply { display(remaining = args.int(REMAINING), maximum = args.int(MAXIMUM)) }
}
