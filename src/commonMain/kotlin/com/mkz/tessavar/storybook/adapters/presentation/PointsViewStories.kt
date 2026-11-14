package com.mkz.tessavar.storybook.adapters.presentation

import com.mkz.tessavar.screen.adapters.presentation.PointsView
import com.mkz.tessavar.storybook.domain.Storybook.Dto.ComponentDto
import com.mkz.tessavar.storybook.domain.Storybook.Dto.ControlDto
import com.mkz.tessavar.storybook.domain.Storybook.Dto.StoryDto
import com.mkz.tessavar.storybook.domain.Storybook.Dto.ValueDto
import korlibs.korge.view.View

object PointsViewStories : StoryBinding {
    private const val MAXIMUM_POINTS = 3
    private const val POINTS_LIMIT = 12

    private const val REMAINING = "remaining"
    private const val MAXIMUM = "maximum"
    private const val COLOR = "color"

    private const val MOVEMENT_COLOR = "FFFFFF"
    private const val CAST_COLOR = "FFBB31"

    private val full = StoryDto(id = "screen-points--full", name = "Full", description = "A battle unit that has spent no step: every point is filled.", args = emptyMap())
    private val partlySpent =
        StoryDto(
            id = "screen-points--partly-spent",
            name = "Partly spent",
            description = "Two out of three points left: the spent one is tinted, with an outline.",
            args =
                mapOf(
                    REMAINING to ValueDto.int(2),
                ),
        )
    private val spent = StoryDto(id = "screen-points--spent", name = "Spent", description = "Every point spent: all of them are tinted, with an outline.", args = mapOf(REMAINING to ValueDto.int(0)))
    private val casts =
        StoryDto(
            id = "screen-points--casts",
            name = "Casts",
            description = "The casts a battle unit has left, in the color of the casts.",
            args =
                mapOf(
                    COLOR to ValueDto.text(CAST_COLOR),
                    MAXIMUM to ValueDto.int(1),
                    REMAINING to ValueDto.int(1),
                ),
        )

    override fun component(context: StoryContext) =
        ComponentDto(
            id = "screen-points",
            group = "Screen",
            title = "Points",
            description = "An amount out of a maximum without text, a rounded pip per point: filled while it remains, tinted once spent. The battle unit info shows its steps and its casts with it.",
            controls = listOf(ControlDto.int(REMAINING, minimum = 0, maximum = POINTS_LIMIT), ControlDto.int(MAXIMUM, minimum = 0, maximum = POINTS_LIMIT), ControlDto.color(COLOR)),
            args = mapOf(REMAINING to ValueDto.int(MAXIMUM_POINTS), MAXIMUM to ValueDto.int(MAXIMUM_POINTS), COLOR to ValueDto.text(MOVEMENT_COLOR)),
            stories = listOf(full, partlySpent, spent, casts),
        )

    override fun render(
        args: Args,
        context: StoryContext,
    ): View = PointsView(args.color(COLOR)).apply { display(remaining = args.int(REMAINING), maximum = args.int(MAXIMUM)) }
}
