package com.mkz.rpg.storybook.adapters.presentation

import com.mkz.rpg.screen.adapters.presentation.HudStyle
import com.mkz.rpg.screen.adapters.presentation.UnitPortraitView
import com.mkz.rpg.storybook.domain.Storybook.Dto.ComponentDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.ControlDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.StoryDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.ValueDto
import korlibs.image.color.RGBA
import korlibs.korge.view.View
import korlibs.math.geom.Size

object UnitPortraitViewStories : StoryBinding {
    private val SIZE = Size(97.5, 97.5)
    private const val DEFAULT_UNIT = "hero"
    private const val OTHER_UNIT = "goat-wizard"

    private const val UNIT = "unit"
    private const val RING = "ring"

    private enum class Ring(
        val color: RGBA?,
    ) {
        NONE(null),
        ALLY(HudStyle.ALLY),
        ENEMY(HudStyle.ENEMY),
        DEFEATED(HudStyle.DEFEATED),
    }

    private val hero = StoryDto(id = "screen-unit-portrait--hero", name = "Hero", description = "The portrait of the hero, in a dark rounded slot.", args = emptyMap())
    private val otherUnit =
        StoryDto(
            id = "screen-unit-portrait--goat-wizard",
            name = "Goat wizard",
            description = "The portrait of another unit: any unit with a portrait can be picked.",
            args =
                mapOf(
                    UNIT to ValueDto.text(OTHER_UNIT),
                ),
        )

    private val ally =
        StoryDto(
            id = "screen-unit-portrait--ally",
            name = "Ally",
            description = "A battle unit of the human player, ringed green.",
            args = mapOf(RING to ValueDto.text(Ring.ALLY.name)),
        )
    private val enemy =
        StoryDto(
            id = "screen-unit-portrait--enemy",
            name = "Enemy",
            description = "A battle unit of the CPU, ringed red.",
            args = mapOf(UNIT to ValueDto.text("rat"), RING to ValueDto.text(Ring.ENEMY.name)),
        )

    override fun component(context: StoryContext) =
        ComponentDto(
            id = "screen-unit-portrait",
            group = "Screen",
            title = "Unit portrait",
            description = "The portrait of a unit in a dark rounded slot, in the info of the selected battle unit, the formation and the previews, ringed with its team's colour in a battle.",
            controls = listOf(ControlDto.pick(UNIT, context.unitIds()), ControlDto.enum(RING, Ring.entries.map { ring -> ring.name })),
            args = mapOf(UNIT to ValueDto.text(DEFAULT_UNIT), RING to ValueDto.text(Ring.NONE.name)),
            stories = listOf(hero, otherUnit, ally, enemy),
        )

    override fun render(
        args: Args,
        context: StoryContext,
    ): View = UnitPortraitView(SIZE, context.resourcesLoader).apply { display(unitId = args.text(UNIT), ring = Ring.valueOf(args.text(RING)).color) }
}
