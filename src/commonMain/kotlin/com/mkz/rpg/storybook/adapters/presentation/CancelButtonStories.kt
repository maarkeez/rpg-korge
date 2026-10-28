package com.mkz.rpg.storybook.adapters.presentation

import com.mkz.rpg.screen.adapters.presentation.CancelButton
import com.mkz.rpg.storybook.domain.Storybook.Dto.ArgDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.ComponentDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.ControlDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.StoryDto
import korlibs.korge.view.View

/** The stories of [CancelButton], the secondary action of the dialogs and the menu, e.g.: Cancel. */
object CancelButtonStories : StoryBinding {
    private const val MINIMUM_WIDTH = 60
    private const val MAXIMUM_WIDTH = 390

    private const val LABEL = "label"
    private const val BUTTON_WIDTH = "buttonWidth"

    private val default = StoryDto(id = "screen-cancel-button--default", name = "Default", description = "The button at the width the dialogs give it.", args = emptyMap())
    private val wide =
        StoryDto(
            id = "screen-cancel-button--wide",
            name = "Wide",
            description = "The button as wide as the screen, as the menu lists its options.",
            args =
                mapOf(
                    BUTTON_WIDTH to ArgDto.int(MAXIMUM_WIDTH),
                ),
        )
    private val longLabel =
        StoryDto(
            id = "screen-cancel-button--long-label",
            name = "Long label",
            description = "A label longer than the button, as a translation may be.",
            args = mapOf(LABEL to ArgDto.text("Cancel and go back to the previous screen")),
        )

    override fun component(context: StoryContext) =
        ComponentDto(
            id = "screen-cancel-button",
            group = "Screen",
            title = "Cancel button",
            description = "A secondary action button in gray, e.g.: Cancel in the dialogs, and the options of the menu other than playing.",
            controls = listOf(ControlDto.text(LABEL), ControlDto.int(BUTTON_WIDTH, minimum = MINIMUM_WIDTH, maximum = MAXIMUM_WIDTH)),
            args = mapOf(LABEL to ArgDto.text("Cancel"), BUTTON_WIDTH to ArgDto.int(190)),
            stories = listOf(default, wide, longLabel),
        )

    override fun render(
        args: Args,
        context: StoryContext,
    ): View = CancelButton(label = args.text(LABEL), buttonWidth = args.int(BUTTON_WIDTH).toDouble(), onCancelled = { context.action("onCancelled") })
}
