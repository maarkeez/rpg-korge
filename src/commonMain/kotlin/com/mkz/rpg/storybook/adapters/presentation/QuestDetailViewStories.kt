package com.mkz.rpg.storybook.adapters.presentation

import com.mkz.rpg.screen.adapters.presentation.QuestDetailView
import com.mkz.rpg.storybook.domain.Storybook.Dto.ComponentDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.ControlDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.StoryDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.ValueDto
import korlibs.korge.view.View

/** The stories of [QuestDetailView], with each kind of quest. */
object QuestDetailViewStories : StoryBinding {
    private const val QUEST = "quest"

    private fun story(
        id: String,
        sample: QuestSample,
    ) = StoryDto(id = "screen-quest-detail--$id", name = sample.title, description = sample.state, args = mapOf(QUEST to ValueDto.text(sample.name)))

    override fun component(context: StoryContext) =
        ComponentDto(
            id = "screen-quest-detail",
            group = "Screen",
            title = "Quest detail",
            description = "A quest in full, in place of the pad, led by who its current task is about, its content evenly apart. Its buttons are in the row of the call to action.",
            controls = listOf(ControlDto.pick(QUEST, QuestSample.entries.map { sample -> sample.name })),
            args = mapOf(QUEST to ValueDto.text(QuestSample.GOAT_WIZARD.name)),
            stories =
                listOf(
                    story("goat-wizard", QuestSample.GOAT_WIZARD),
                    story("defeats-counted", QuestSample.DEFEATS_COUNTED),
                    story("talk", QuestSample.TALK),
                    story("visit", QuestSample.VISIT),
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
        QuestDetailView(context.resourcesLoader, context.messages).apply {
            display(QuestSample.valueOf(args.text(QUEST)).quest(context.messages))
        }
}
