package com.mkz.rpg.storybook.adapters.presentation

import com.mkz.rpg.screen.adapters.presentation.GiveGoldActionsView
import com.mkz.rpg.storybook.domain.Storybook.Dto.ArgDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.ComponentDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.ControlDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.StoryDto
import korlibs.korge.view.View

/** The stories of [GiveGoldActionsView], at the width of the screen, with the texts of the language global. */
object GiveGoldActionsViewStories : StoryBinding {
    private const val MAXIMUM_GOLD = 99_999

    private const val AMOUNT = "amount"
    private const val MAXIMUM = "maximum"

    private val default = StoryDto(id = "screen-give-gold-actions--default", name = "Default", description = "Below the panel: Cancel, All and Give, which reads the amount.", args = emptyMap())
    private val atTheMaximum =
        StoryDto(
            id = "screen-give-gold-actions--at-the-maximum",
            name = "At the maximum",
            description = "The amount is the most that can be given: All would change nothing, so it is faded.",
            args = mapOf(AMOUNT to ArgDto.int(120)),
        )

    override fun component(context: StoryContext) =
        ComponentDto(
            id = "screen-give-gold-actions",
            group = "Screen",
            title = "Give gold actions",
            description = "Cancel, All and Give, at the level of the finish turn, while the panel to give gold is displayed.",
            controls = listOf(ControlDto.int(AMOUNT, minimum = 1, maximum = MAXIMUM_GOLD), ControlDto.int(MAXIMUM, minimum = 1, maximum = MAXIMUM_GOLD)),
            args = mapOf(AMOUNT to ArgDto.int(25), MAXIMUM to ArgDto.int(120)),
            stories = listOf(default, atTheMaximum),
        )

    override fun render(
        args: Args,
        context: StoryContext,
    ): View =
        GiveGoldActionsView(
            context.messages,
            onCancelled = { context.action("onCancelled") },
            onAll = { context.action("onAll", setArg = AMOUNT to ArgDto.int(args.int(MAXIMUM))) },
            onGiven = { context.action("onGiven") },
        ).apply { displayAmount(args.int(AMOUNT).coerceAtMost(args.int(MAXIMUM)), args.int(MAXIMUM)) }
}
