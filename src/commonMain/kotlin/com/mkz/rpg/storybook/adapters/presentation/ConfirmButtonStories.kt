package com.mkz.rpg.storybook.adapters.presentation

import com.mkz.rpg.screen.adapters.presentation.ConfirmButton
import com.mkz.rpg.storybook.domain.Storybook.Dto.ArgDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.ComponentDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.ControlDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.StoryDto
import korlibs.korge.view.View

/** The stories of [ConfirmButton], the primary action of the dialogs and the menu, e.g.: Confirm. */
object ConfirmButtonStories : StoryBinding {
    private const val MINIMUM_WIDTH = 60
    private const val MAXIMUM_WIDTH = 390

    private const val LABEL = "label"
    private const val BUTTON_WIDTH = "buttonWidth"

    private val default = StoryDto(id = "screen-confirm-button--default", name = "Default", description = "The button at the width the dialogs give it.", args = emptyMap())
    private val wide =
        StoryDto(
            id = "screen-confirm-button--wide",
            name = "Wide",
            description = "The button as wide as the screen, as the menu lists its options.",
            args =
                mapOf(
                    BUTTON_WIDTH to ArgDto.int(MAXIMUM_WIDTH),
                ),
        )
    private val longLabel =
        StoryDto(
            id = "screen-confirm-button--long-label",
            name = "Long label",
            description = "A label longer than the button, as a translation may be.",
            args = mapOf(LABEL to ArgDto.text("Confirm and leave the shop for good")),
        )

    override fun component(context: StoryContext) =
        ComponentDto(
            id = "screen-confirm-button",
            group = "Screen",
            title = "Confirm button",
            description = "A primary action button in blue, e.g.: Confirm in the dialogs, and Play new game in the menu.",
            controls = listOf(ControlDto.text(LABEL), ControlDto.int(BUTTON_WIDTH, minimum = MINIMUM_WIDTH, maximum = MAXIMUM_WIDTH)),
            args = mapOf(LABEL to ArgDto.text("Confirm"), BUTTON_WIDTH to ArgDto.int(190)),
            stories = listOf(default, wide, longLabel),
        )

    override fun render(
        args: Args,
        context: StoryContext,
    ): View = ConfirmButton(label = args.text(LABEL), buttonWidth = args.int(BUTTON_WIDTH).toDouble(), onConfirmed = { context.action("onConfirmed") })
}
