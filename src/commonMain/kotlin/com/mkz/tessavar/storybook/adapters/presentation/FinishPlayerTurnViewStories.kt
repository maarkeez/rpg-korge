package com.mkz.tessavar.storybook.adapters.presentation

import com.mkz.tessavar.screen.adapters.presentation.FinishPlayerTurnView
import com.mkz.tessavar.storybook.domain.Storybook.Dto.ComponentDto
import com.mkz.tessavar.storybook.domain.Storybook.Dto.ControlDto
import com.mkz.tessavar.storybook.domain.Storybook.Dto.StoryDto
import com.mkz.tessavar.storybook.domain.Storybook.Dto.ValueDto
import korlibs.korge.view.View

object FinishPlayerTurnViewStories : StoryBinding {
    private const val ENABLED = "enabled"

    private val enabled = StoryDto(id = "screen-finish-turn--enabled", name = "Enabled", description = "The player's turn: the button ends it.", args = emptyMap())
    private val disabled =
        StoryDto(
            id = "screen-finish-turn--disabled",
            name = "Disabled",
            description = "The button faded, as the player can not end the turn for now: tapping it does nothing.",
            args = mapOf(ENABLED to ValueDto.boolean(false)),
        )

    override fun component(context: StoryContext) =
        ComponentDto(
            id = "screen-finish-turn",
            group = "Screen",
            title = "Finish turn",
            description = "The button that ends the player's turn in a battle, under the battlefield.",
            controls = listOf(ControlDto.boolean(ENABLED)),
            args = mapOf(ENABLED to ValueDto.boolean(true)),
            stories = listOf(enabled, disabled),
        )

    override fun render(
        args: Args,
        context: StoryContext,
    ): View =
        FinishPlayerTurnView(context.messages, onPlayerTurnFinished = { context.action("onPlayerTurnFinished") }).apply {
            isEnabled = args.boolean(ENABLED)
        }
}
