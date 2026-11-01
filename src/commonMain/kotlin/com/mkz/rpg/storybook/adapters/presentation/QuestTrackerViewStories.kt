package com.mkz.rpg.storybook.adapters.presentation

import com.mkz.rpg.screen.adapters.presentation.QuestTrackerView
import com.mkz.rpg.storybook.domain.Storybook.Dto.ArgDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.ComponentDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.ControlDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.StoryDto
import korlibs.korge.view.View

/** The stories of [QuestTrackerView], with each kind of quest. Its pop is an animation: the stories show how it settles. Tapping it logs it. */
object QuestTrackerViewStories : StoryBinding {
    private const val QUEST = "quest"
    private const val TAPPABLE = "tappable"

    private fun story(
        id: String,
        sample: QuestSample,
    ) = StoryDto(id = "screen-quest-tracker--$id", name = sample.title, description = sample.state, args = mapOf(QUEST to ArgDto.text(sample.name)))

    private val readOnly =
        StoryDto(
            id = "screen-quest-tracker--read-only",
            name = "Read only",
            description = "As in a battle: no chevron, and tapping it does nothing.",
            args = mapOf(TAPPABLE to ArgDto.boolean(false)),
        )

    override fun component(context: StoryContext) =
        ComponentDto(
            id = "screen-quest-tracker",
            group = "Screen",
            title = "Quest tracker",
            description = "A capsule on the right of the top line, naming the current task of the pinned quest, or that the main quest is completed.",
            controls = listOf(ControlDto.pick(QUEST, QuestSample.entries.map { sample -> sample.name }), ControlDto.boolean(TAPPABLE)),
            args = mapOf(QUEST to ArgDto.text(QuestSample.GOAT_WIZARD.name), TAPPABLE to ArgDto.boolean(true)),
            stories =
                listOf(
                    story("goat-wizard", QuestSample.GOAT_WIZARD),
                    story("counted", QuestSample.COUNTED),
                    story("talk", QuestSample.TALK),
                    story("reach", QuestSample.REACH),
                    story("long-text", QuestSample.LONG_TEXT),
                    story("completed", QuestSample.COMPLETED),
                    story("pinned", QuestSample.PINNED),
                    story("pinned-with-long-text", QuestSample.PINNED_WITH_LONG_TEXT),
                    readOnly,
                ),
        )

    override fun render(
        args: Args,
        context: StoryContext,
    ): View =
        QuestTrackerView(context.texts).apply {
            setDelegate(
                object : QuestTrackerView.Delegate {
                    override fun questTrackerSelected() = context.action("questTrackerSelected")
                },
            )
            setTappable(args.boolean(TAPPABLE))
            display(QuestSample.valueOf(args.text(QUEST)).quest(context.texts))
        }
}
