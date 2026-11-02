package com.mkz.rpg.storybook.adapters.presentation

import com.mkz.rpg.screen.adapters.presentation.QuestDetailView
import com.mkz.rpg.storybook.domain.Storybook.Dto.ArgDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.ComponentDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.ControlDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.StoryDto
import korlibs.korge.view.View

/** The stories of [QuestDetailView], with each kind of quest. */
object QuestDetailViewStories : StoryBinding {
    private const val QUEST = "quest"

    private fun story(
        id: String,
        sample: QuestSample,
    ) = StoryDto(id = "screen-quest-detail--$id", name = sample.title, description = sample.state, args = mapOf(QUEST to ArgDto.text(sample.name)))

    override fun component(context: StoryContext) =
        ComponentDto(
            id = "screen-quest-detail",
            group = "Screen",
            title = "Quest detail",
            description = "A quest in full, in place of the pad, led by who its current task is about, its content evenly apart. Its buttons are in the row of the call to action.",
            controls = listOf(ControlDto.pick(QUEST, QuestSample.entries.map { sample -> sample.name })),
            args = mapOf(QUEST to ArgDto.text(QuestSample.GOAT_WIZARD.name)),
            stories =
                listOf(
                    story("goat-wizard", QuestSample.GOAT_WIZARD),
                    story("counted", QuestSample.COUNTED),
                    story("talk", QuestSample.TALK),
                    story("reach", QuestSample.REACH),
                    story("several-tasks", QuestSample.SEVERAL_TASKS),
                    story("item-rewards", QuestSample.ITEM_REWARDS),
                    story("long-text", QuestSample.LONG_TEXT),
                    story("completed", QuestSample.COMPLETED),
                    story("side-quest", QuestSample.SIDE_QUEST),
                    story("repeatable", QuestSample.REPEATABLE),
                    story("completed-repeatable", QuestSample.COMPLETED_REPEATABLE),
                    story("large-reward", QuestSample.LARGE_REWARD),
                ),
        )

    override fun render(
        args: Args,
        context: StoryContext,
    ): View =
        QuestDetailView(context.resourcesLoader, context.texts).apply {
            display(QuestSample.valueOf(args.text(QUEST)).quest(context.texts))
        }
}
