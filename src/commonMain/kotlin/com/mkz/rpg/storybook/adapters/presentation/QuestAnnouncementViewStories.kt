package com.mkz.rpg.storybook.adapters.presentation

import com.mkz.rpg.screen.adapters.presentation.QuestAnnouncementView
import com.mkz.rpg.screen.adapters.presentation.QuestAnnouncementView.Moment
import com.mkz.rpg.storybook.domain.Storybook.Dto.ArgDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.ComponentDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.ControlDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.StoryDto
import korlibs.korge.view.View

/** The stories of [QuestAnnouncementView], at the start and the end of a quest. Continue logs itself. */
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
        args = mapOf(MOMENT to ArgDto.text(moment.name), QUEST to ArgDto.text(sample.name)),
    )

    override fun component(context: StoryContext) =
        ComponentDto(
            id = "screen-quest-announcement",
            group = "Screen",
            title = "Quest announcement",
            description = "A card over the dimmed screen, until Continue is tapped, announcing that the main quest started, or completed with its reward.",
            controls = listOf(ControlDto.enum(MOMENT, Moment.entries.map { moment -> moment.name }), ControlDto.pick(QUEST, QuestSample.entries.map { sample -> sample.name })),
            args = mapOf(MOMENT to ArgDto.text(Moment.STARTED.name), QUEST to ArgDto.text(QuestSample.GOAT_WIZARD.name)),
            stories =
                listOf(
                    story("started", "Started", Moment.STARTED, QuestSample.GOAT_WIZARD),
                    story("completed", "Completed", Moment.COMPLETED, QuestSample.COMPLETED),
                    story("completed-with-items", "Completed with items", Moment.COMPLETED, QuestSample.ITEM_REWARDS),
                    story("started-with-long-text", "Started with long text", Moment.STARTED, QuestSample.LONG_TEXT),
                    story("completed-with-long-text", "Completed with long text", Moment.COMPLETED, QuestSample.LONG_TEXT),
                ),
        )

    override fun render(
        args: Args,
        context: StoryContext,
    ): View =
        QuestAnnouncementView(context.resourcesLoader, context.texts).apply {
            setDelegate(
                object : QuestAnnouncementView.Delegate {
                    override fun questAnnouncementContinued() = context.action("questAnnouncementContinued")
                },
            )
            display(Moment.valueOf(args.text(MOMENT)), QuestSample.valueOf(args.text(QUEST)).quest(context.texts))
        }
}
