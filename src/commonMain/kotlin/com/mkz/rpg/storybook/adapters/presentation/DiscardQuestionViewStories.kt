package com.mkz.rpg.storybook.adapters.presentation

import com.mkz.rpg.screen.adapters.presentation.DiscardQuestionView
import com.mkz.rpg.storybook.domain.Storybook.Dto.ComponentDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.ControlDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.StoryDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.ValueDto
import korlibs.korge.view.View

/** The stories of [DiscardQuestionView], with the texts of the language global. Tapping a button records itself. */
object DiscardQuestionViewStories : StoryBinding {
    private const val ITEM_NAME = "itemName"

    private val question = StoryDto(id = "screen-discard-question--question", name = "Question", description = "The player tapped Discard, or a stored item to swap.", args = emptyMap())
    private val longestName =
        StoryDto(
            id = "screen-discard-question--longest-name",
            name = "Longest name",
            description = "The longest item name has to fit the line, in every language.",
            args = mapOf(ITEM_NAME to ValueDto.text("Esencia de teletransporte")),
        )

    override fun component(context: StoryContext) =
        ComponentDto(
            id = "screen-discard-question",
            group = "Screen",
            title = "Discard question",
            description = "Asks whether to discard an item, in place of its actions, with Back and Discard in red.",
            controls = listOf(ControlDto.text(ITEM_NAME)),
            args = mapOf(ITEM_NAME to ValueDto.text("Health Potion")),
            stories = listOf(question, longestName),
        )

    override fun render(
        args: Args,
        context: StoryContext,
    ): View =
        DiscardQuestionView(
            context.messages,
            itemName = args.text(ITEM_NAME),
            onBack = { context.action("onBack") },
            onDiscarded = { context.action("onDiscarded") },
        )
}
