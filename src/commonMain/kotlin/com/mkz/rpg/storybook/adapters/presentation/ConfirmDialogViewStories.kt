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

    // The questions the game asks, each with its message, and its secondary and primary choices.
    private enum class Question(
        val message: UiText,
        val cancel: UiText,
        val confirm: UiText,
    ) {
        QUIT(message = UiText.PAUSE_QUIT_CONFIRMATION, cancel = UiText.CALL_TO_ACTION_CANCEL, confirm = UiText.PAUSE_QUIT),
    }

    private val quit =
        StoryDto(
            id = "screen-confirm-dialog--quit",
            name = "Quit",
            description = "Asked before quitting to the menu from the pause screen, as the progress since the last save is lost.",
            args = mapOf(QUESTION to ArgDto.text(Question.QUIT.name.lowercase())),
        )

    override fun component(context: StoryContext) =
        ComponentDto(
            id = "screen-confirm-dialog",
            group = "Screen",
            title = "Confirm dialog",
            description = "A question displayed in place of the pad before an action that can not be undone, over a secondary and a primary choice.",
            controls = listOf(ControlDto.pick(QUESTION, Question.entries.map { question -> question.name.lowercase() })),
            args = mapOf(QUESTION to ArgDto.text(Question.QUIT.name.lowercase())),
            stories = listOf(quit),
        )

    override fun render(
        args: Args,
        context: StoryContext,
    ): View {
        val question = Question.valueOf(args.text(QUESTION).uppercase())
        return ConfirmDialogView().apply {
            display(
                message = context.texts[question.message],
                cancelLabel = context.texts[question.cancel],
                confirmLabel = context.texts[question.confirm],
                onCancelled = { context.action("onCancelled") },
                onConfirmed = { context.action("onConfirmed") },
            )
        }
    }
}
