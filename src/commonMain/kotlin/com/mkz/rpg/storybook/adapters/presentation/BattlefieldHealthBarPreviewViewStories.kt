package com.mkz.rpg.storybook.adapters.presentation

import com.mkz.rpg.screen.adapters.presentation.BattlefieldHealthBarPreviewView
import com.mkz.rpg.screen.adapters.presentation.BattlefieldHealthBarPreviewView.PreviewOption
import com.mkz.rpg.screen.adapters.presentation.BattlefieldView
import com.mkz.rpg.storybook.domain.Storybook.Dto.ArgDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.ComponentDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.ControlDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.StoryDto
import korlibs.korge.view.View
import korlibs.math.geom.Size

/** The stories of [BattlefieldHealthBarPreviewView], at the size the battlefield displays it over a tile. */
object BattlefieldHealthBarPreviewViewStories : StoryBinding {
    // A tile wide, and a fifth of a tile high.
    private val SIZE = Size(BattlefieldView.TILE_SIZE, BattlefieldView.TILE_SIZE / 5.0)

    private const val PREVIEW = "preview"

    private val full =
        StoryDto(id = "screen-battlefield-health-bar-preview--full", name = "Full", description = "A battle unit that would keep all its health: the three thirds stay still.", args = emptyMap())
    private val threeOutOfThree =
        StoryDto(
            id = "screen-battlefield-health-bar-preview--three-out-of-three",
            name = "Three out of three",
            description = "A battle unit that would lose a little: its last third blinks.",
            args = mapOf(PREVIEW to ArgDto.text(PreviewOption.THREE_OUT_OF_THREE.name)),
        )
    private val twoOutOfThree =
        StoryDto(
            id = "screen-battlefield-health-bar-preview--two-out-of-three",
            name = "Two out of three",
            description = "A battle unit that would keep two thirds of its health: the second third blinks.",
            args = mapOf(PREVIEW to ArgDto.text(PreviewOption.TWO_OUT_OF_THREE.name)),
        )
    private val oneOutOfThree =
        StoryDto(
            id = "screen-battlefield-health-bar-preview--one-out-of-three",
            name = "One out of three",
            description = "A battle unit that would keep a third of its health: the first third blinks.",
            args = mapOf(PREVIEW to ArgDto.text(PreviewOption.ONE_OUT_OF_THREE.name)),
        )
    private val empty =
        StoryDto(
            id = "screen-battlefield-health-bar-preview--empty",
            name = "Empty",
            description = "A battle unit that would be defeated: its first third blinks out.",
            args = mapOf(PREVIEW to ArgDto.text(PreviewOption.EMPTY.name)),
        )

    override fun component(context: StoryContext) =
        ComponentDto(
            id = "screen-battlefield-health-bar-preview",
            group = "Screen",
            title = "Battlefield health bar preview",
            description = "The health a battle unit would have left after a cast, in thirds of its maximum health, over its tile of the battlefield.",
            controls = listOf(ControlDto.pick(PREVIEW, PreviewOption.entries.map { option -> option.name })),
            args = mapOf(PREVIEW to ArgDto.text(PreviewOption.FULL.name)),
            stories = listOf(full, threeOutOfThree, twoOutOfThree, oneOutOfThree, empty),
        )

    override fun render(
        args: Args,
        context: StoryContext,
    ): View = BattlefieldHealthBarPreviewView(SIZE).apply { display(PreviewOption.valueOf(args.text(PREVIEW))) }
}
