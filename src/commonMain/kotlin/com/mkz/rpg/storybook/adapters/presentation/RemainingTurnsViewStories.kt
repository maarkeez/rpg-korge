package com.mkz.rpg.storybook.adapters.presentation

import com.mkz.rpg.screen.adapters.presentation.RemainingTurnsView
import com.mkz.rpg.storybook.domain.Storybook.Dto.ArgDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.ComponentDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.ControlDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.StoryDto
import korlibs.korge.view.View
import korlibs.math.geom.Size

/** The stories of [RemainingTurnsView], at the size the battle unit info displays it, in the language of the globals. */
object RemainingTurnsViewStories : StoryBinding {
    private val SIZE = Size(244, 14)
    private const val TURNS_LIMIT = 99

    private const val TURNS = "turns"

    private val severalTurns = StoryDto(id = "screen-remaining-turns--several-turns", name = "Several turns", description = "A summon that vanishes in three turns.", args = emptyMap())
    private val lastTurn =
        StoryDto(
            id = "screen-remaining-turns--last-turn",
            name = "Last turn",
            description = "A summon on its last turn: the text is in the singular.",
            args =
                mapOf(
                    TURNS to ArgDto.int(1),
                ),
        )

    override fun component(context: StoryContext) =
        ComponentDto(
            id = "screen-remaining-turns",
            group = "Screen",
            title = "Remaining turns",
            description = "How many turns a summon has left before it vanishes, the current one included, in the info of the selected battle unit.",
            controls = listOf(ControlDto.int(TURNS, minimum = 1, maximum = TURNS_LIMIT)),
            args = mapOf(TURNS to ArgDto.int(3)),
            stories = listOf(severalTurns, lastTurn),
        )

    override fun render(
        args: Args,
        context: StoryContext,
    ): View = RemainingTurnsView(SIZE, context.texts).apply { display(remainingTurns = args.int(TURNS)) }
}
