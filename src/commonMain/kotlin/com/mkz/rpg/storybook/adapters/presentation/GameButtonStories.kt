package com.mkz.rpg.storybook.adapters.presentation

import com.mkz.rpg.shared.adapters.presentation.GameButton
import com.mkz.rpg.storybook.domain.Storybook.Dto.ArgDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.ComponentDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.ControlDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.StoryDto
import korlibs.korge.input.onClick
import korlibs.korge.view.View
import korlibs.math.geom.Size

/** The stories of [GameButton], at the size of the buttons of the inventory, in any color. */
object GameButtonStories : StoryBinding {
    private val SIZE = Size(120, 32)

    private const val LABEL = "label"
    private const val ENABLED = "enabled"
    private const val COLOR = "color"

    private const val GREY = "696969"
    private const val FINISH_TURN_BLUE = "0088FF"

    private val enabled = StoryDto(id = "shared-game-button--enabled", name = "Enabled", description = "A button that can be tapped: grey, with a white label.", args = emptyMap())
    private val disabled =
        StoryDto(
            id = "shared-game-button--disabled",
            name = "Disabled",
            description = "A button that can not be tapped, faded: its background dark, its label grey and its border dim.",
            args = mapOf(ENABLED to ArgDto.boolean(false)),
        )
    private val otherColor =
        StoryDto(
            id = "shared-game-button--other-color",
            name = "Other color",
            description = "The blue of Finish turn instead of the grey: a button of any color fades the same once disabled.",
            args = mapOf(LABEL to ArgDto.text("Finish turn"), COLOR to ArgDto.text(FINISH_TURN_BLUE)),
        )

    override fun component(context: StoryContext) =
        ComponentDto(
            id = "shared-game-button",
            group = "Shared",
            title = "Game button",
            description = "The button of the game, e.g.: the arrows, Close and the item actions. A disabled one is still displayed, but faded, so the player tells it can not be tapped.",
            controls = listOf(ControlDto.text(LABEL), ControlDto.boolean(ENABLED), ControlDto.color(COLOR)),
            args = mapOf(LABEL to ArgDto.text("Close"), ENABLED to ArgDto.boolean(true), COLOR to ArgDto.text(GREY)),
            stories = listOf(enabled, disabled, otherColor),
        )

    override fun render(
        args: Args,
        context: StoryContext,
    ): View =
        GameButton(SIZE, args.text(LABEL), color = args.color(COLOR)).apply {
            enabled = args.boolean(ENABLED)
            onClick { context.action("onClick") }
        }
}
