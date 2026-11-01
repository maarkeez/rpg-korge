package com.mkz.rpg.storybook.adapters.presentation

import com.mkz.rpg.screen.adapters.presentation.ConfirmDialogView
import com.mkz.rpg.screen.adapters.presentation.UiText
import com.mkz.rpg.storybook.domain.Storybook.Dto.ArgDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.ComponentDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.ControlDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.StoryDto
import korlibs.korge.view.View

/** The stories of [ConfirmDialogView], with the questions the game asks, built with its real buttons. */
object ConfirmDialogViewStories : StoryBinding {
    private const val QUESTION = "question"
    private const val QUEST_NAME = "Bats in the Meadow"

    // The questions the game asks, each with its message, and its secondary and primary choices.
    private enum class Question(
        val message: UiText,
        val cancel: UiText,
        val confirm: UiText,
    ) {
        QUIT(message = UiText.PAUSE_QUIT_CONFIRMATION, cancel = UiText.CALL_TO_ACTION_CANCEL, confirm = UiText.PAUSE_QUIT),
        OVERWRITE(message = UiText.SAVE_SLOTS_OVERWRITE_CONFIRMATION, cancel = UiText.CALL_TO_ACTION_CANCEL, confirm = UiText.SAVE_SLOTS_OVERWRITE),
        DELETE(message = UiText.SAVE_SLOTS_DELETE_CONFIRMATION, cancel = UiText.CALL_TO_ACTION_CANCEL, confirm = UiText.SAVE_SLOTS_DELETE),
        ABANDON(message = UiText.QUEST_ABANDON_QUESTION, cancel = UiText.QUEST_DETAIL_BACK, confirm = UiText.QUEST_ABANDON),
    }

    private val delete =
        StoryDto(
            id = "screen-confirm-dialog--delete",
            name = "Delete",
            description = "Asked before deleting the game saved in slot 2, from Load game.",
            args = mapOf(QUESTION to ArgDto.text(Question.DELETE.name.lowercase())),
        )

    private val overwrite =
        StoryDto(
            id = "screen-confirm-dialog--overwrite",
            name = "Overwrite",
            description = "Asked before saving into slot 2, which holds a game, as that game is lost.",
            args = mapOf(QUESTION to ArgDto.text(Question.OVERWRITE.name.lowercase())),
        )

    private val quit =
        StoryDto(
            id = "screen-confirm-dialog--quit",
            name = "Quit",
            description = "Asked before quitting to the menu from the pause screen, as the progress since the last save is lost.",
            args = mapOf(QUESTION to ArgDto.text(Question.QUIT.name.lowercase())),
        )

    private val abandon =
        StoryDto(
            id = "screen-confirm-dialog--abandon",
            name = "Abandon",
            description = "Asked before abandoning a side quest from its detail view, as its progress is lost.",
            args = mapOf(QUESTION to ArgDto.text(Question.ABANDON.name.lowercase())),
        )

    override fun component(context: StoryContext) =
        ComponentDto(
            id = "screen-confirm-dialog",
            group = "Screen",
            title = "Confirm dialog",
            description = "A question displayed in place of the pad before an action that can not be undone, over a secondary and a primary choice.",
            controls = listOf(ControlDto.pick(QUESTION, Question.entries.map { question -> question.name.lowercase() })),
            args = mapOf(QUESTION to ArgDto.text(Question.QUIT.name.lowercase())),
            stories = listOf(overwrite, delete, quit, abandon),
        )

    override fun render(
        args: Args,
        context: StoryContext,
    ): View {
        val question = Question.valueOf(args.text(QUESTION).uppercase())
        return ConfirmDialogView().apply {
            display(
                message = context.texts[question.message, "slot" to 2, "quest" to QUEST_NAME],
                cancelLabel = context.texts[question.cancel],
                confirmLabel = context.texts[question.confirm],
                onCancelled = { context.action("onCancelled") },
                onConfirmed = { context.action("onConfirmed") },
            )
        }
    }
}
