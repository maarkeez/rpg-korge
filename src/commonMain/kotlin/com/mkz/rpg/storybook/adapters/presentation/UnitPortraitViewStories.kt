package com.mkz.rpg.storybook.adapters.presentation

import com.mkz.rpg.screen.adapters.presentation.UnitPortraitView
import com.mkz.rpg.storybook.domain.Storybook.Dto.ArgDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.ComponentDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.ControlDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.StoryDto
import korlibs.korge.view.View
import korlibs.math.geom.Size

/** The stories of [UnitPortraitView], at the size the battle unit info displays it. */
object UnitPortraitViewStories : StoryBinding {
    private val SIZE = Size(97.5, 97.5)
    private const val DEFAULT_UNIT = "hero"
    private const val OTHER_UNIT = "goat-wizard"

    private const val UNIT = "unit"

    private val hero = StoryDto(id = "screen-unit-portrait--hero", name = "Hero", description = "The portrait of the hero, in a white frame.", args = emptyMap())
    private val otherUnit =
        StoryDto(
            id = "screen-unit-portrait--goat-wizard",
            name = "Goat wizard",
            description = "The portrait of another unit: any unit with a portrait can be picked.",
            args =
                mapOf(
                    UNIT to ArgDto.text(OTHER_UNIT),
                ),
        )

    override fun component(context: StoryContext) =
        ComponentDto(
            id = "screen-unit-portrait",
            group = "Screen",
            title = "Unit portrait",
            description = "The portrait of a unit, in the info of the selected battle unit, the formation and the previews.",
            controls = listOf(ControlDto.pick(UNIT, context.unitIds())),
            args = mapOf(UNIT to ArgDto.text(DEFAULT_UNIT)),
            stories = listOf(hero, otherUnit),
        )

    override fun render(
        args: Args,
        context: StoryContext,
    ): View = UnitPortraitView(SIZE, context.resourcesLoader).apply { display(unitId = args.text(UNIT)) }
}
