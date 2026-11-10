package com.mkz.rpg.storybook.adapters.presentation

import com.mkz.rpg.screen.adapters.presentation.BattlefieldView
import com.mkz.rpg.screen.adapters.presentation.QuestMarkerView
import com.mkz.rpg.storybook.domain.Storybook.Dto.ComponentDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.ControlDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.StoryDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.ValueDto
import korlibs.korge.view.View

object QuestMarkerViewStories : StoryBinding {
    private const val SIGN = "sign"
    private const val NPC_ROW = 2
    private const val NPC_COLUMN = 0

    private fun story(
        id: String,
        name: String,
        description: String,
        sign: QuestMarkerView.Sign,
    ) = StoryDto(id = "screen-quest-marker--$id", name = name, description = description, args = mapOf(SIGN to ValueDto.text(sign.name)))

    override fun component(context: StoryContext) =
        ComponentDto(
            id = "screen-quest-marker",
            group = "Screen",
            title = "Quest marker",
            description = "The sign over an npc's head while exploring: gold when it can start a side quest, and green when a current task is to talk to it.",
            controls = listOf(ControlDto.enum(SIGN, QuestMarkerView.Sign.entries.map { sign -> sign.name })),
            args = mapOf(SIGN to ValueDto.text(QuestMarkerView.Sign.QUEST_TO_START.name)),
            stories =
                listOf(
                    story("quest-to-start", "Quest to start", "The npc can start a side quest.", QuestMarkerView.Sign.QUEST_TO_START),
                    story("talk-task", "Talk task", "A current task is to talk to the npc.", QuestMarkerView.Sign.TALK_TASK),
                ),
        )

    override fun render(
        args: Args,
        context: StoryContext,
    ): View =
        (BattlefieldViewStories.render(Args(BattlefieldViewStories.component(context).args), context) as BattlefieldView).apply {
            displayQuestMarker(NPC_ROW, NPC_COLUMN, QuestMarkerView.Sign.valueOf(args.text(SIGN)))
        }
}
