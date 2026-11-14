package com.mkz.tessavar.storybook.adapters.presentation

import com.mkz.tessavar.screen.adapters.presentation.GiveGoldRefusalView
import com.mkz.tessavar.screen.adapters.presentation.UiMessage
import com.mkz.tessavar.storybook.domain.Storybook.Dto.ComponentDto
import com.mkz.tessavar.storybook.domain.Storybook.Dto.ControlDto
import com.mkz.tessavar.storybook.domain.Storybook.Dto.StoryDto
import com.mkz.tessavar.storybook.domain.Storybook.Dto.ValueDto
import korlibs.korge.view.View

object GiveGoldRefusalViewStories : StoryBinding {
    private const val REASON = "reason"

    private val noGold = StoryDto(id = "screen-give-gold-refusal--no-gold", name = "No gold", description = "The giver carries no gold.", args = emptyMap())
    private val noOne =
        StoryDto(
            id = "screen-give-gold-refusal--no-one",
            name = "No one",
            description = "The giver is alone in the party.",
            args = mapOf(REASON to ValueDto.text(UiMessage.GIVE_GOLD_NO_ONE.name)),
        )
    private val goldFull =
        StoryDto(
            id = "screen-give-gold-refusal--maximum-gold",
            name = "Maximum gold",
            description = "Every other party member carries the most gold possible.",
            args = mapOf(REASON to ValueDto.text(UiMessage.GIVE_GOLD_GOLD_FULL.name)),
        )

    override fun component(context: StoryContext) =
        ComponentDto(
            id = "screen-give-gold-refusal",
            group = "Screen",
            title = "Give gold refusal",
            description = "Why no gold can be given, with OK, read after tapping the faded gold button.",
            controls = listOf(ControlDto.enum(REASON, listOf(UiMessage.GIVE_GOLD_NO_GOLD.name, UiMessage.GIVE_GOLD_NO_ONE.name, UiMessage.GIVE_GOLD_GOLD_FULL.name))),
            args = mapOf(REASON to ValueDto.text(UiMessage.GIVE_GOLD_NO_GOLD.name)),
            stories = listOf(noGold, noOne, goldFull),
        )

    override fun render(
        args: Args,
        context: StoryContext,
    ): View = GiveGoldRefusalView(context.messages[UiMessage.valueOf(args.text(REASON))], context.messages[UiMessage.GIVE_GOLD_OK], onDismissed = { context.action("onDismissed") })
}
