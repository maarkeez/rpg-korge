package com.mkz.rpg.storybook.adapters.presentation

import com.mkz.rpg.screen.adapters.presentation.QuestJournalView
import com.mkz.rpg.storybook.domain.Storybook.Dto.ComponentDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.ControlDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.StoryDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.ValueDto
import korlibs.korge.view.View

object QuestJournalViewStories : StoryBinding {
    private const val COUNT = "count"
    private const val SCROLL = "scroll"
    private const val LONG_TEXT = "longText"
    private const val MINIMUM_COUNT = 2
    private const val MAXIMUM_COUNT = 12
    private const val MAXIMUM_SCROLL = 1000
    private const val DRAGGED_SCROLL = 100

    private fun story(
        id: String,
        name: String,
        description: String,
        count: Int,
        scroll: Int = 0,
        isLong: Boolean = false,
    ) = StoryDto(
        id = "screen-quest-journal--$id",
        name = name,
        description = description,
        args = mapOf(COUNT to ValueDto.int(count), SCROLL to ValueDto.int(scroll), LONG_TEXT to ValueDto.boolean(isLong)),
    )

    override fun component(context: StoryContext) =
        ComponentDto(
            id = "screen-quest-journal",
            group = "Screen",
            title = "Quest journal",
            description = "Every quest started, in place of the pad, the active ones first and the completed ones ticked below them, dragged to scroll. Its Back is in the row of the call to action.",
            controls =
                listOf(
                    ControlDto.int(COUNT, minimum = MINIMUM_COUNT, maximum = MAXIMUM_COUNT),
                    ControlDto.int(SCROLL, minimum = 0, maximum = MAXIMUM_SCROLL),
                    ControlDto.boolean(LONG_TEXT),
                ),
            args = mapOf(COUNT to ValueDto.int(MINIMUM_COUNT), SCROLL to ValueDto.int(0), LONG_TEXT to ValueDto.boolean(false)),
            stories =
                listOf(
                    story("two-quests", "Two quests", "The main quest and a side quest: it all fits.", count = 2),
                    story("six-quests", "Six quests", "More than fit: the list scrolls, with its scrollbar.", count = 6),
                    story("twelve-quests", "Twelve quests", "Active and completed, with a repeatable one in each.", count = 12),
                    story("twelve-quests-scrolled", "Twelve quests, scrolled", "At the end of the list, with the completed ones ticked.", count = 12, scroll = MAXIMUM_SCROLL),
                    story("twelve-quests-dragged", "Twelve quests, dragged", "Halfway through a drag: the rows crossing the edges of the list are cut there.", count = 12, scroll = DRAGGED_SCROLL),
                    story("long-text", "Long text", "The longest texts, as a translation may be: cut before what ends the row.", count = 4, isLong = true),
                ),
        )

    override fun render(
        args: Args,
        context: StoryContext,
    ): View =
        QuestJournalView(context.resourcesLoader, context.messages).apply {
            setDelegate(
                object : QuestJournalView.Delegate {
                    override fun questJournalSelected(questId: String) = context.action("questJournalSelected", questId)

                    override fun questJournalPinSelected(questId: String) = context.action("questJournalPinSelected", questId)
                },
            )
            display(QuestSample.journal(args.int(COUNT), args.boolean(LONG_TEXT), context.messages))
            scrollTo(args.int(SCROLL).toDouble())
        }
}
