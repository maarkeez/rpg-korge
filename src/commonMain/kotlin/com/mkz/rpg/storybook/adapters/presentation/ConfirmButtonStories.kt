package com.mkz.rpg.storybook.adapters.presentation

import com.mkz.rpg.screen.adapters.presentation.ConfirmButton
import com.mkz.rpg.screen.adapters.presentation.HudStyle
import com.mkz.rpg.storybook.domain.Storybook.Dto.ComponentDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.ControlDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.StoryDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.ValueDto
import korlibs.korge.view.View

object ConfirmButtonStories : StoryBinding {
    private const val MINIMUM_WIDTH = 60
    private const val MAXIMUM_WIDTH = 390

    private const val LABEL = "label"
    private const val BUTTON_WIDTH = "buttonWidth"
    private const val GOLD = "gold"

    private val default = StoryDto(id = "screen-confirm-button--default", name = "Default", description = "The button at the width the dialogs give it.", args = emptyMap())
    private val wide =
        StoryDto(
            id = "screen-confirm-button--wide",
            name = "Wide",
            description = "The button as wide as the screen, as the menu lists its options.",
            args =
                mapOf(
                    BUTTON_WIDTH to ValueDto.int(MAXIMUM_WIDTH),
                ),
        )
    private val longLabel =
        StoryDto(
            id = "screen-confirm-button--long-label",
            name = "Long label",
            description = "A label longer than the button, as a translation may be.",
            args = mapOf(LABEL to ValueDto.text("Confirm and leave the shop for good")),
        )
    private val gold =
        StoryDto(
            id = "screen-confirm-button--gold",
            name = "Gold",
            description = "The button in coin gold with dark text, as Wishlist on the menu, opening the signup form of the mailing list.",
            args = mapOf(LABEL to ValueDto.text("Wishlist"), BUTTON_WIDTH to ValueDto.int(250), GOLD to ValueDto.boolean(true)),
        )

    override fun component(context: StoryContext) =
        ComponentDto(
            id = "screen-confirm-button",
            group = "Screen",
            title = "Confirm button",
            description = "A primary action button in blue, e.g.: Confirm in the dialogs, and Play new game in the menu, or in gold, as Wishlist in the menu.",
            controls = listOf(ControlDto.text(LABEL), ControlDto.int(BUTTON_WIDTH, minimum = MINIMUM_WIDTH, maximum = MAXIMUM_WIDTH), ControlDto.boolean(GOLD)),
            args = mapOf(LABEL to ValueDto.text("Confirm"), BUTTON_WIDTH to ValueDto.int(190), GOLD to ValueDto.boolean(false)),
            stories = listOf(default, wide, longLabel, gold),
        )

    override fun render(
        args: Args,
        context: StoryContext,
    ): View =
        if (args.boolean(GOLD)) {
            ConfirmButton(
                label = args.text(LABEL),
                buttonWidth = args.int(BUTTON_WIDTH).toDouble(),
                color = HudStyle.COIN_MIDDLE,
                pressedColor = HudStyle.COIN_PRESSED,
                textColor = HudStyle.PANEL,
                onConfirmed = { context.action("onConfirmed") },
            )
        } else {
            ConfirmButton(label = args.text(LABEL), buttonWidth = args.int(BUTTON_WIDTH).toDouble(), onConfirmed = { context.action("onConfirmed") })
        }
}
