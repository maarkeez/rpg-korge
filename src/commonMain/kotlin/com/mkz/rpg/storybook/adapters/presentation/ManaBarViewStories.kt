package com.mkz.rpg.storybook.adapters.presentation

import com.mkz.rpg.screen.adapters.presentation.ManaBarView
import com.mkz.rpg.storybook.domain.Storybook.Dto.ArgDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.ComponentDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.ControlDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.StoryDto
import korlibs.korge.view.View
import korlibs.math.geom.Size

/** The stories of [ManaBarView], at the size the battle unit info displays it. */
object ManaBarViewStories : StoryBinding {
    private val SIZE = Size(281.5, 16)
    private const val MAXIMUM_MANA = 20
    private const val MANA_LIMIT = 999

    private const val REMAINING = "remaining"
    private const val MAXIMUM = "maximum"

    private val full = StoryDto(id = "screen-mana-bar--full", name = "Full", description = "A battle unit that has cast no ability.", args = emptyMap())
    private val half =
        StoryDto(id = "screen-mana-bar--half", name = "Half", description = "A battle unit that has spent half of its mana.", args = mapOf(REMAINING to ArgDto.int(MAXIMUM_MANA / 2)))
    private val empty = StoryDto(id = "screen-mana-bar--empty", name = "Empty", description = "A battle unit with no mana left.", args = mapOf(REMAINING to ArgDto.int(0)))

    override fun component(context: StoryContext) =
        ComponentDto(
            id = "screen-mana-bar",
            group = "Screen",
            title = "Mana bar",
            description = "The mana a battle unit has left, out of its maximum, in the info of the selected battle unit and in the formation.",
            controls = listOf(ControlDto.int(REMAINING, minimum = 0, maximum = MANA_LIMIT), ControlDto.int(MAXIMUM, minimum = 1, maximum = MANA_LIMIT)),
            args = mapOf(REMAINING to ArgDto.int(MAXIMUM_MANA), MAXIMUM to ArgDto.int(MAXIMUM_MANA)),
            stories = listOf(full, half, empty),
        )

    override fun render(
        args: Args,
        context: StoryContext,
    ): View = ManaBarView(SIZE).apply { display(remaining = args.int(REMAINING), maximum = args.int(MAXIMUM)) }
}
