package com.mkz.rpg.storybook.adapters.presentation

import com.mkz.rpg.screen.adapters.presentation.QuestTrackerView
import com.mkz.rpg.storybook.domain.Storybook.Dto.ComponentDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.ControlDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.StoryDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.ValueDto
import korlibs.korge.view.View

/** The stories of [QuestTrackerView], with each kind of quest, and as the empty tracker. Its pop is an animation: the stories show how it settles. Tapping it records it. */
object QuestTrackerViewStories : StoryBinding {
    private const val QUEST = "quest"
    private const val TAPPABLE = "tappable"

    // The quest picked to draw the empty tracker instead of a quest.
    private const val EMPTY = "EMPTY"

    private fun story(
        id: String,
        sample: QuestSample,
    ) = StoryDto(id = "screen-quest-tracker--$id", name = sample.title, description = sample.state, args = mapOf(QUEST to ValueDto.text(sample.name)))

    private val empty =
        StoryDto(
            id = "screen-quest-tracker--empty",
            name = "Empty",
            description = "While a side quest is active, none is pinned and the main quest has not started: tapping it opens the journal.",
            args = mapOf(QUEST to ValueDto.text(EMPTY)),
        )

    private val readOnly =
        StoryDto(
            id = "screen-quest-tracker--read-only",
            name = "Read only",
            description = "As in a battle: no chevron, and tapping it does nothing.",
            args = mapOf(TAPPABLE to ValueDto.boolean(false)),
        )

    override fun component(context: StoryContext) =
        ComponentDto(
            id = "screen-quest-tracker",
            group = "Screen",
            title = "Quest tracker",
            description = "A capsule on the right of the top line, naming the current task of the pinned quest, or that the main quest is completed, or that no quest is pinned.",
            controls = listOf(ControlDto.pick(QUEST, QuestSample.entries.map { sample -> sample.name } + EMPTY), ControlDto.boolean(TAPPABLE)),
            args = mapOf(QUEST to ValueDto.text(QuestSample.GOAT_WIZARD.name), TAPPABLE to ValueDto.boolean(true)),
            stories =
                listOf(
                    story("goat-wizard", QuestSample.GOAT_WIZARD),
                    story("defeats-counted", QuestSample.DEFEATS_COUNTED),
                    story("talk", QuestSample.TALK),
                    story("visit", QuestSample.VISIT),
                    story("long-text", QuestSample.LONG_TEXT),
                    story("completed", QuestSample.COMPLETED),
                    story("pinned", QuestSample.PINNED),
                    story("pinned-with-long-text", QuestSample.PINNED_WITH_LONG_TEXT),
                    empty,
                    readOnly,
                ),
        )

    override fun render(
        args: Args,
        context: StoryContext,
    ): View =
        QuestTrackerView(context.messages).apply {
            setDelegate(
                object : QuestTrackerView.Delegate {
                    override fun questTrackerSelected() = context.action("questTrackerSelected")
                },
            )
            setTappable(args.boolean(TAPPABLE))
            val quest = args.text(QUEST)
            if (quest == EMPTY) displayEmpty() else display(QuestSample.valueOf(quest).quest(context.messages))
        }
}
