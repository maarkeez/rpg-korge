package com.mkz.rpg.storybook.adapters.presentation

import com.mkz.rpg.screen.adapters.presentation.PointsView
import com.mkz.rpg.storybook.domain.Storybook.Dto.ArgDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.ComponentDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.ControlDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.StoryDto
import korlibs.korge.view.View

/** The stories of [PointsView], in the colors the battle unit info gives the steps and the casts. */
object PointsViewStories : StoryBinding {
    private const val MAXIMUM_POINTS = 3
    private const val POINTS_LIMIT = 12

    private const val REMAINING = "remaining"
    private const val MAXIMUM = "maximum"
    private const val COLOR = "color"

    // The colors of the remaining steps and the remaining casts of the battle unit info.
    private const val MOVEMENT_COLOR = "FFFFFF"
    private const val CAST_COLOR = "FFBB31"

    private val full = StoryDto(id = "screen-points--full", name = "Full", description = "A battle unit that has spent no step: every point is filled.", args = emptyMap())
    private val partlySpent =
        StoryDto(id = "screen-points--partly-spent", name = "Partly spent", description = "Two out of three points left: the spent one is hollow.", args = mapOf(REMAINING to ArgDto.int(2)))
    private val spent = StoryDto(id = "screen-points--spent", name = "Spent", description = "Every point spent: all of them are hollow.", args = mapOf(REMAINING to ArgDto.int(0)))
    private val casts =
        StoryDto(
            id = "screen-points--casts",
            name = "Casts",
            description = "The casts a battle unit has left, in the color of the casts.",
            args =
                mapOf(
                    COLOR to ArgDto.text(CAST_COLOR),
                    MAXIMUM to ArgDto.int(1),
                    REMAINING to ArgDto.int(1),
                ),
        )

    override fun component(context: StoryContext) =
        ComponentDto(
            id = "screen-points",
            group = "Screen",
            title = "Points",
            description = "An amount out of a maximum without text, a circle per point: filled while it remains, hollow once spent. The battle unit info shows its steps and its casts with it.",
            controls = listOf(ControlDto.int(REMAINING, minimum = 0, maximum = POINTS_LIMIT), ControlDto.int(MAXIMUM, minimum = 0, maximum = POINTS_LIMIT), ControlDto.color(COLOR)),
            args = mapOf(REMAINING to ArgDto.int(MAXIMUM_POINTS), MAXIMUM to ArgDto.int(MAXIMUM_POINTS), COLOR to ArgDto.text(MOVEMENT_COLOR)),
            stories = listOf(full, partlySpent, spent, casts),
        )

    override fun render(
        args: Args,
        context: StoryContext,
    ): View = PointsView(args.color(COLOR)).apply { display(remaining = args.int(REMAINING), maximum = args.int(MAXIMUM)) }
}
