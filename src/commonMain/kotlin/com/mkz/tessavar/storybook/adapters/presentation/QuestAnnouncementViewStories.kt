package com.mkz.tessavar.storybook.adapters.presentation

import com.mkz.tessavar.screen.adapters.presentation.QuestAnnouncementView
import com.mkz.tessavar.screen.adapters.presentation.QuestAnnouncementView.Moment
import com.mkz.tessavar.storybook.domain.Storybook.Dto.ComponentDto
import com.mkz.tessavar.storybook.domain.Storybook.Dto.ControlDto
import com.mkz.tessavar.storybook.domain.Storybook.Dto.StoryDto
import com.mkz.tessavar.storybook.domain.Storybook.Dto.ValueDto
import korlibs.korge.view.View

object QuestAnnouncementViewStories : StoryBinding {
    private const val MOMENT = "moment"
    private const val QUEST = "quest"

    private fun story(
        id: String,
        name: String,
        moment: Moment,
        sample: QuestSample,
    ) = StoryDto(
        id = "screen-quest-announcement--$id",
        name = name,
        description = sample.state,
        args = mapOf(MOMENT to ValueDto.text(moment.name), QUEST to ValueDto.text(sample.name)),
    )

    override fun component(context: StoryContext) =
        ComponentDto(
            id = "screen-quest-announcement",
            group = "Screen",
            title = "Quest announcement",
            description = "A toast over the top of the battlefield, which fades by itself and takes no taps, announcing that a quest started, or completed with its reward.",
            controls = listOf(ControlDto.enum(MOMENT, Moment.entries.map { moment -> moment.name }), ControlDto.pick(QUEST, QuestSample.entries.map { sample -> sample.name })),
            args = mapOf(MOMENT to ValueDto.text(Moment.STARTED.name), QUEST to ValueDto.text(QuestSample.GOAT_WIZARD.name)),
            stories =
                listOf(
                    story("started", "Started", Moment.STARTED, QuestSample.GOAT_WIZARD),
                    story("completed", "Completed", Moment.COMPLETED, QuestSample.COMPLETED),
                    story("completed-with-items", "Completed with items", Moment.COMPLETED, QuestSample.ITEM_REWARDS),
                    story("large-reward", "Large reward", Moment.COMPLETED, QuestSample.LARGE_REWARD),
                    story("started-with-long-text", "Started with long text", Moment.STARTED, QuestSample.LONG_TEXT),
                    story("completed-with-long-text", "Completed with long text", Moment.COMPLETED, QuestSample.LONG_TEXT),
                ),
        )

    override fun render(
        args: Args,
        context: StoryContext,
    ): View =
        QuestAnnouncementView(context.resourcesLoader, context.messages).apply {
            display(Moment.valueOf(args.text(MOMENT)), QuestSample.valueOf(args.text(QUEST)).quest(context.messages))
        }
}
