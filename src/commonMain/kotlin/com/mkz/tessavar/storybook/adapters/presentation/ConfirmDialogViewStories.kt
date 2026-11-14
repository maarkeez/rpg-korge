package com.mkz.tessavar.storybook.adapters.presentation

import com.mkz.tessavar.screen.adapters.presentation.ConfirmDialogView
import com.mkz.tessavar.screen.adapters.presentation.UiMessage
import com.mkz.tessavar.storybook.domain.Storybook.Dto.ComponentDto
import com.mkz.tessavar.storybook.domain.Storybook.Dto.ControlDto
import com.mkz.tessavar.storybook.domain.Storybook.Dto.StoryDto
import com.mkz.tessavar.storybook.domain.Storybook.Dto.ValueDto
import korlibs.korge.view.View

object ConfirmDialogViewStories : StoryBinding {
    private const val QUESTION = "question"
    private const val QUEST_NAME = "Bats in the Meadow"

    private enum class Question(
        val message: UiMessage,
    ) {
        QUIT(UiMessage.PAUSE_QUIT_CONFIRMATION),
        OVERWRITE(UiMessage.SAVE_SLOTS_OVERWRITE_CONFIRMATION),
        DELETE(UiMessage.SAVE_SLOTS_DELETE_CONFIRMATION),
        ABANDON(UiMessage.QUEST_ABANDON_QUESTION),
    }

    private val delete =
        StoryDto(
            id = "screen-confirm-dialog--delete",
            name = "Delete",
            description = "Asked before deleting the game saved in slot 2, from Load game.",
            args = mapOf(QUESTION to ValueDto.text(Question.DELETE.name.lowercase())),
        )

    private val overwrite =
        StoryDto(
            id = "screen-confirm-dialog--overwrite",
            name = "Overwrite",
            description = "Asked before saving into slot 2, which holds a game, as that game is lost.",
            args = mapOf(QUESTION to ValueDto.text(Question.OVERWRITE.name.lowercase())),
        )

    private val quit =
        StoryDto(
            id = "screen-confirm-dialog--quit",
            name = "Quit",
            description = "Asked before quitting to the menu from the pause screen, as the progress since the last save is lost.",
            args = mapOf(QUESTION to ValueDto.text(Question.QUIT.name.lowercase())),
        )

    private val abandon =
        StoryDto(
            id = "screen-confirm-dialog--abandon",
            name = "Abandon",
            description = "Asked before abandoning a side quest from its detail view, as its progress is lost.",
            args = mapOf(QUESTION to ValueDto.text(Question.ABANDON.name.lowercase())),
        )

    override fun component(context: StoryContext) =
        ComponentDto(
            id = "screen-confirm-dialog",
            group = "Screen",
            title = "Confirm dialog",
            description = "A question displayed in place of the pad before an action that can not be undone. Its choices are in the row of the call to action.",
            controls = listOf(ControlDto.pick(QUESTION, Question.entries.map { question -> question.name.lowercase() })),
            args = mapOf(QUESTION to ValueDto.text(Question.QUIT.name.lowercase())),
            stories = listOf(overwrite, delete, quit, abandon),
        )

    override fun render(
        args: Args,
        context: StoryContext,
    ): View {
        val question = Question.valueOf(args.text(QUESTION).uppercase())
        return ConfirmDialogView().apply { display(context.messages[question.message, "slot" to 2, "quest" to QUEST_NAME]) }
    }
}
