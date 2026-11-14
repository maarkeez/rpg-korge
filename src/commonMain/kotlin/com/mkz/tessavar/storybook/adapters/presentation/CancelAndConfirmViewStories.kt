package com.mkz.tessavar.storybook.adapters.presentation

import com.mkz.tessavar.screen.adapters.presentation.CancelAndConfirmView
import com.mkz.tessavar.storybook.domain.Storybook.Dto.ComponentDto
import com.mkz.tessavar.storybook.domain.Storybook.Dto.ControlDto
import com.mkz.tessavar.storybook.domain.Storybook.Dto.StoryDto
import com.mkz.tessavar.storybook.domain.Storybook.Dto.ValueDto
import korlibs.korge.view.View

object CancelAndConfirmViewStories : StoryBinding {
    private const val CANCEL_LABEL = "cancelLabel"
    private const val CONFIRM_LABEL = "confirmLabel"

    private val default = StoryDto(id = "screen-cancel-and-confirm--default", name = "Default", description = "Cancel on the left and Confirm on the right.", args = emptyMap())
    private val exportAndBack =
        StoryDto(
            id = "screen-cancel-and-confirm--export-and-back",
            name = "Export and back",
            description = "The labels the world builder gives it to export the world or go back to the menu.",
            args = mapOf(CANCEL_LABEL to ValueDto.text("Back"), CONFIRM_LABEL to ValueDto.text("Export")),
        )
    private val leaveConfirmation =
        StoryDto(
            id = "screen-cancel-and-confirm--leave-confirmation",
            name = "Leave confirmation",
            description = "The labels the world builder gives it to confirm leaving, as the world is lost.",
            args = mapOf(CANCEL_LABEL to ValueDto.text("Stay"), CONFIRM_LABEL to ValueDto.text("Leave")),
        )
    private val longLabels =
        StoryDto(
            id = "screen-cancel-and-confirm--long-labels",
            name = "Long labels",
            description = "Labels longer than the buttons, as translations may be.",
            args = mapOf(CANCEL_LABEL to ValueDto.text("Cancel and go back"), CONFIRM_LABEL to ValueDto.text("Confirm and continue")),
        )

    override fun component(context: StoryContext) =
        ComponentDto(
            id = "screen-cancel-and-confirm",
            group = "Screen",
            title = "Cancel and confirm",
            description = "A secondary action on the left and a primary one on the right, as the call to action of the world builder shows them.",
            controls = listOf(ControlDto.text(CANCEL_LABEL), ControlDto.text(CONFIRM_LABEL)),
            args = mapOf(CANCEL_LABEL to ValueDto.text("Cancel"), CONFIRM_LABEL to ValueDto.text("Confirm")),
            stories = listOf(default, exportAndBack, leaveConfirmation, longLabels),
        )

    override fun render(
        args: Args,
        context: StoryContext,
    ): View =
        CancelAndConfirmView(
            onCancelled = { context.action("onCancelled") },
            onConfirmed = { context.action("onConfirmed") },
            cancelLabel = args.text(CANCEL_LABEL),
            confirmLabel = args.text(CONFIRM_LABEL),
        )
}
