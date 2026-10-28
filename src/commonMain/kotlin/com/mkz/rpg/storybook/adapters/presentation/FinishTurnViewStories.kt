package com.mkz.rpg.storybook.adapters.presentation

import com.mkz.rpg.screen.adapters.presentation.FinishTurnView
import com.mkz.rpg.storybook.domain.Storybook.Dto.ArgDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.ComponentDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.ControlDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.StoryDto
import korlibs.korge.view.View

/** The stories of [FinishTurnView], at the width of the screen, with the text of the language global. */
object FinishTurnViewStories : StoryBinding {
    private const val ENABLED = "enabled"

    private val enabled = StoryDto(id = "screen-finish-turn--enabled", name = "Enabled", description = "The player's turn: the button ends it.", args = emptyMap())
    private val disabled =
        StoryDto(
            id = "screen-finish-turn--disabled",
            name = "Disabled",
            description = "The button greyed out, as the player can not end the turn for now: tapping it does nothing.",
            args = mapOf(ENABLED to ArgDto.boolean(false)),
        )

    override fun component(context: StoryContext) =
        ComponentDto(
            id = "screen-finish-turn",
            group = "Screen",
            title = "Finish turn",
            description = "The button that ends the player's turn in a battle, under the battlefield.",
            controls = listOf(ControlDto.boolean(ENABLED)),
            args = mapOf(ENABLED to ArgDto.boolean(true)),
            stories = listOf(enabled, disabled),
        )

    override fun render(
        args: Args,
        context: StoryContext,
    ): View =
        FinishTurnView(context.texts, onTurnFinished = { context.action("onTurnFinished") }).apply {
            isEnabled = args.boolean(ENABLED)
        }
}
