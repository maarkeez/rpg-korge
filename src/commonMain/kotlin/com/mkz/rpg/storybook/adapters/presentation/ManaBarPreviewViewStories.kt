package com.mkz.rpg.storybook.adapters.presentation

import com.mkz.rpg.screen.adapters.presentation.ManaBarPreviewView
import com.mkz.rpg.storybook.domain.Storybook.Dto.ArgDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.ComponentDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.ControlDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.StoryDto
import korlibs.korge.view.View
import korlibs.math.geom.Size

/** The stories of [ManaBarPreviewView], at the size the attack and receiver previews display it. */
object ManaBarPreviewViewStories : StoryBinding {
    private val SIZE = Size(281.5, 16)
    private const val MAXIMUM_MANA = 20
    private const val MANA_LIMIT = 999

    private const val BEFORE = "remainingBefore"
    private const val AFTER = "remainingAfter"
    private const val MAXIMUM = "maximum"

    private val unchanged = StoryDto(id = "screen-mana-bar-preview--unchanged", name = "Unchanged", description = "A cast that costs no mana.", args = emptyMap())
    private val spent =
        StoryDto(
            id = "screen-mana-bar-preview--spent",
            name = "Spent",
            description = "The mana the caster would have left after the cast, over the mana it has now.",
            args = mapOf(BEFORE to ArgDto.int(MAXIMUM_MANA), AFTER to ArgDto.int(MAXIMUM_MANA / 4)),
        )
    private val drained =
        StoryDto(
            id = "screen-mana-bar-preview--drained",
            name = "Drained",
            description = "A cast that takes all the mana the caster has left.",
            args = mapOf(BEFORE to ArgDto.int(MAXIMUM_MANA / 2), AFTER to ArgDto.int(0)),
        )
    private val restored =
        StoryDto(
            id = "screen-mana-bar-preview--restored",
            name = "Restored",
            description = "A potion on a battle unit short of mana: the mana it would have after it, over the mana it has now.",
            args = mapOf(BEFORE to ArgDto.int(MAXIMUM_MANA / 4), AFTER to ArgDto.int(MAXIMUM_MANA)),
        )

    override fun component(context: StoryContext) =
        ComponentDto(
            id = "screen-mana-bar-preview",
            group = "Screen",
            title = "Mana bar preview",
            description = "The mana a battle unit would have left after a cast, over the mana it has now, in the attack and receiver previews.",
            controls =
                listOf(
                    ControlDto.int(BEFORE, minimum = 0, maximum = MANA_LIMIT),
                    ControlDto.int(AFTER, minimum = 0, maximum = MANA_LIMIT),
                    ControlDto.int(MAXIMUM, minimum = 1, maximum = MANA_LIMIT),
                ),
            args = mapOf(BEFORE to ArgDto.int(MAXIMUM_MANA), AFTER to ArgDto.int(MAXIMUM_MANA), MAXIMUM to ArgDto.int(MAXIMUM_MANA)),
            stories = listOf(unchanged, spent, drained, restored),
        )

    override fun render(
        args: Args,
        context: StoryContext,
    ): View = ManaBarPreviewView(SIZE).apply { display(remainingBefore = args.int(BEFORE), remainingAfter = args.int(AFTER), maximum = args.int(MAXIMUM)) }
}
