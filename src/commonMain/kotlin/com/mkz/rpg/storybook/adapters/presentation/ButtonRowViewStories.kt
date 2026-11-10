package com.mkz.rpg.storybook.adapters.presentation

import com.mkz.rpg.screen.adapters.presentation.ButtonRowView
import com.mkz.rpg.screen.adapters.presentation.ConfirmButton
import com.mkz.rpg.screen.adapters.presentation.HudStyle
import com.mkz.rpg.storybook.domain.Storybook.Dto.ComponentDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.ControlDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.StoryDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.ValueDto
import korlibs.korge.view.View

object ButtonRowViewStories : StoryBinding {
    private const val FIRST = "first"
    private const val SECOND = "second"
    private const val THIRD = "third"
    private const val LAST_IS_PRIMARY = "lastIsPrimary"
    private const val LAST_IS_DANGER = "lastIsDanger"
    private const val LAST_IS_ENABLED = "lastIsEnabled"

    private fun story(
        id: String,
        name: String,
        description: String,
        vararg args: Pair<String, ValueDto>,
    ) = StoryDto(id = "screen-button-row--$id", name = name, description = description, args = mapOf(*args))

    override fun component(context: StoryContext) =
        ComponentDto(
            id = "screen-button-row",
            group = "Screen",
            title = "Button row",
            description = "The buttons of a panel, at the level of the finish turn, sharing its width equally. An empty label draws no button.",
            controls =
                listOf(
                    ControlDto.text(FIRST),
                    ControlDto.text(SECOND),
                    ControlDto.text(THIRD),
                    ControlDto.boolean(LAST_IS_PRIMARY),
                    ControlDto.boolean(LAST_IS_DANGER),
                    ControlDto.boolean(LAST_IS_ENABLED),
                ),
            args =
                mapOf(
                    FIRST to ValueDto.text("Back"),
                    SECOND to ValueDto.text(""),
                    THIRD to ValueDto.text(""),
                    LAST_IS_PRIMARY to ValueDto.boolean(false),
                    LAST_IS_DANGER to ValueDto.boolean(false),
                    LAST_IS_ENABLED to ValueDto.boolean(true),
                ),
            stories =
                listOf(
                    story("one-button", "One button", "A lone button fills the row, e.g.: Back of the journal."),
                    story("two-buttons", "Two buttons", "Two buttons share it in halves, e.g.: Back and Buy in the shop.", SECOND to ValueDto.text("Buy"), LAST_IS_PRIMARY to ValueDto.boolean(true)),
                    story("danger", "Danger", "An action that can not be undone, in red, e.g.: Abandon.", SECOND to ValueDto.text("Abandon"), LAST_IS_DANGER to ValueDto.boolean(true)),
                    story(
                        "three-buttons",
                        "Three buttons",
                        "Three buttons share it in thirds, e.g.: the merchant's choices.",
                        FIRST to ValueDto.text("Leave"),
                        SECOND to ValueDto.text("Sell"),
                        THIRD to ValueDto.text("Buy"),
                        LAST_IS_PRIMARY to ValueDto.boolean(true),
                    ),
                    story(
                        "disabled-button",
                        "Disabled button",
                        "An action that can not be done, faded, reading why, e.g.: in the shop.",
                        SECOND to ValueDto.text("Not enough gold"),
                        LAST_IS_ENABLED to ValueDto.boolean(false),
                    ),
                ),
        )

    override fun render(
        args: Args,
        context: StoryContext,
    ): View {
        val labels = listOf(args.text(FIRST), args.text(SECOND), args.text(THIRD)).filter { label -> label.isNotEmpty() }
        return ButtonRowView(
            labels.mapIndexed { index, label ->
                val isLast = index == labels.lastIndex
                val onSelected = { context.action("onSelected", label) }
                when {
                    isLast && args.boolean(LAST_IS_DANGER) -> ButtonRowView.Action(label, HudStyle.DANGER, HudStyle.DANGER_PRESSED, onSelected = onSelected)
                    isLast && args.boolean(LAST_IS_PRIMARY) -> ButtonRowView.Action(label, ConfirmButton.COLOR, ConfirmButton.PRESSED_COLOR, onSelected = onSelected)
                    else -> ButtonRowView.Action(label, isEnabled = !isLast || args.boolean(LAST_IS_ENABLED), onSelected = onSelected)
                }
            },
        )
    }
}
