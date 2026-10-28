package com.mkz.rpg.storybook.adapters.presentation

import com.mkz.rpg.screen.adapters.presentation.CancelAndConfirmView
import com.mkz.rpg.storybook.domain.Storybook.Dto.ArgDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.ComponentDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.ControlDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.StoryDto
import korlibs.korge.view.View

/** The stories of [CancelAndConfirmView], built with its real [com.mkz.rpg.screen.adapters.presentation.CancelButton] and [com.mkz.rpg.screen.adapters.presentation.ConfirmButton]. */
object CancelAndConfirmViewStories : StoryBinding {
    private const val CANCEL_LABEL = "cancelLabel"
    private const val CONFIRM_LABEL = "confirmLabel"

    private val default = StoryDto(id = "screen-cancel-and-confirm--default", name = "Default", description = "Cancel on the left and Confirm on the right.", args = emptyMap())
    private val exportAndBack =
        StoryDto(
            id = "screen-cancel-and-confirm--export-and-back",
            name = "Export and back",
            description = "The labels the map builder gives it to export the map or go back to the menu.",
            args = mapOf(CANCEL_LABEL to ArgDto.text("Back"), CONFIRM_LABEL to ArgDto.text("Export")),
        )
    private val leaveConfirmation =
        StoryDto(
            id = "screen-cancel-and-confirm--leave-confirmation",
            name = "Leave confirmation",
            description = "The labels the map builder gives it to confirm leaving, as the map is lost.",
            args = mapOf(CANCEL_LABEL to ArgDto.text("Stay"), CONFIRM_LABEL to ArgDto.text("Leave")),
        )
    private val longLabels =
        StoryDto(
            id = "screen-cancel-and-confirm--long-labels",
            name = "Long labels",
            description = "Labels longer than the buttons, as translations may be.",
            args = mapOf(CANCEL_LABEL to ArgDto.text("Cancel and go back"), CONFIRM_LABEL to ArgDto.text("Confirm and continue")),
        )

    override fun component(context: StoryContext) =
        ComponentDto(
            id = "screen-cancel-and-confirm",
            group = "Screen",
            title = "Cancel and confirm",
            description = "A secondary action on the left and a primary one on the right, as the call to action of the map builder shows them.",
            controls = listOf(ControlDto.text(CANCEL_LABEL), ControlDto.text(CONFIRM_LABEL)),
            args = mapOf(CANCEL_LABEL to ArgDto.text("Cancel"), CONFIRM_LABEL to ArgDto.text("Confirm")),
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
