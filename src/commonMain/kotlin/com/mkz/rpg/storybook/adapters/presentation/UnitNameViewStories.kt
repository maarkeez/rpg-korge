package com.mkz.rpg.storybook.adapters.presentation

import com.mkz.rpg.screen.adapters.presentation.UnitNameView
import com.mkz.rpg.storybook.domain.Storybook.Dto.ArgDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.ComponentDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.ControlDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.StoryDto
import korlibs.korge.view.View
import korlibs.math.geom.Size

/** The stories of [UnitNameView], at the size the battle unit info displays it. */
object UnitNameViewStories : StoryBinding {
    private val SIZE = Size(244, 14)
    private const val NAME = "Hero"

    private const val UNIT_NAME = "unitName"
    private const val ALIGNED_TO_RIGHT = "alignedToRight"

    private val short = StoryDto(id = "screen-unit-name--short", name = "Short", description = "The name of a battle unit, on the left.", args = emptyMap())
    private val alignedToRight =
        StoryDto(
            id = "screen-unit-name--aligned-to-right",
            name = "Aligned to right",
            description = "The name on the right, as the receiver preview and the amounts of gold and prices display it.",
            args = mapOf(ALIGNED_TO_RIGHT to ArgDto.boolean(true)),
        )
    private val long =
        StoryDto(
            id = "screen-unit-name--long",
            name = "Long",
            description = "A name longer than the view is wide.",
            args =
                mapOf(
                    UNIT_NAME to ArgDto.text("The Unbelievably Magnificent Viking Warrior of the North"),
                ),
        )

    override fun component(context: StoryContext) =
        ComponentDto(
            id = "screen-unit-name",
            group = "Screen",
            title = "Unit name",
            description = "A line of text in the info of a battle unit, e.g.: its name. The inventory, the shop and the item actions show their titles, gold and prices with it too.",
            controls = listOf(ControlDto.text(UNIT_NAME), ControlDto.boolean(ALIGNED_TO_RIGHT)),
            args = mapOf(UNIT_NAME to ArgDto.text(NAME), ALIGNED_TO_RIGHT to ArgDto.boolean(false)),
            stories = listOf(short, alignedToRight, long),
        )

    override fun render(
        args: Args,
        context: StoryContext,
    ): View =
        UnitNameView(SIZE).apply {
            if (args.boolean(ALIGNED_TO_RIGHT)) alignTextToRight()
            display(unitName = args.text(UNIT_NAME))
        }
}
