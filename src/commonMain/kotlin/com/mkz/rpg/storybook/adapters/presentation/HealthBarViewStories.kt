package com.mkz.rpg.storybook.adapters.presentation

import com.mkz.rpg.screen.adapters.presentation.HealthBarView
import com.mkz.rpg.storybook.domain.Storybook.Dto.ArgDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.ComponentDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.ControlDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.StoryDto
import korlibs.korge.view.View
import korlibs.math.geom.Size

/** The stories of [HealthBarView], at the size the battle unit info displays it. */
object HealthBarViewStories : StoryBinding {
    private val SIZE = Size(281.5, 16)
    private const val MAXIMUM_HEALTH = 30
    private const val HEALTH_LIMIT = 999

    private const val REMAINING = "remaining"
    private const val MAXIMUM = "maximum"

    private val full = StoryDto(id = "screen-health-bar--full", name = "Full", description = "A battle unit that has not been hurt.", args = emptyMap())
    private val half =
        StoryDto(id = "screen-health-bar--half", name = "Half", description = "A battle unit that has lost half of its health.", args = mapOf(REMAINING to ArgDto.int(MAXIMUM_HEALTH / 2)))
    private val empty = StoryDto(id = "screen-health-bar--empty", name = "Empty", description = "A battle unit that has been defeated.", args = mapOf(REMAINING to ArgDto.int(0)))

    override fun component(context: StoryContext) =
        ComponentDto(
            id = "screen-health-bar",
            group = "Screen",
            title = "Health bar",
            description = "The health a battle unit has left, out of its maximum, in the info of the selected battle unit.",
            controls = listOf(ControlDto.int(REMAINING, minimum = 0, maximum = HEALTH_LIMIT), ControlDto.int(MAXIMUM, minimum = 1, maximum = HEALTH_LIMIT)),
            args = mapOf(REMAINING to ArgDto.int(MAXIMUM_HEALTH), MAXIMUM to ArgDto.int(MAXIMUM_HEALTH)),
            stories = listOf(full, half, empty),
        )

    override fun render(
        args: Args,
        context: StoryContext,
    ): View = HealthBarView(SIZE).apply { display(remaining = args.int(REMAINING), maximum = args.int(MAXIMUM)) }
}
