package com.mkz.rpg.storybook.adapters.presentation

import com.mkz.rpg.screen.adapters.presentation.ActionRowView
import com.mkz.rpg.screen.adapters.presentation.HudStyle
import com.mkz.rpg.storybook.domain.Storybook.Dto.ArgDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.ComponentDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.ControlDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.StoryDto
import korlibs.korge.view.View

/** The stories of [ActionRowView], from a lone button to three. Tapping a button logs itself. */
object ActionRowViewStories : StoryBinding {
    private const val FIRST = "first"
    private const val SECOND = "second"
    private const val THIRD = "third"
    private const val LAST_IS_DANGER = "lastIsDanger"
    private const val LAST_IS_ENABLED = "lastIsEnabled"

    private fun story(
        id: String,
        name: String,
        description: String,
        vararg args: Pair<String, ArgDto>,
    ) = StoryDto(id = "screen-action-row--$id", name = name, description = description, args = mapOf(*args))

    override fun component(context: StoryContext) =
        ComponentDto(
            id = "screen-action-row",
            group = "Screen",
            title = "Action row",
            description = "The buttons of a panel, at the level of the finish turn, sharing its width equally. An empty label draws no button.",
            controls =
                listOf(
                    ControlDto.text(FIRST),
                    ControlDto.text(SECOND),
                    ControlDto.text(THIRD),
                    ControlDto.boolean(LAST_IS_DANGER),
                    ControlDto.boolean(LAST_IS_ENABLED),
                ),
            args =
                mapOf(
                    FIRST to ArgDto.text("Back"),
                    SECOND to ArgDto.text(""),
                    THIRD to ArgDto.text(""),
                    LAST_IS_DANGER to ArgDto.boolean(false),
                    LAST_IS_ENABLED to ArgDto.boolean(true),
                ),
            stories =
                listOf(
                    story("one-action", "One action", "A lone button fills the row, e.g.: Back of the journal."),
                    story("two-actions", "Two actions", "Two buttons share it in halves, e.g.: Back and Buy in the shop.", SECOND to ArgDto.text("Buy")),
                    story("danger", "Danger", "An action that can not be undone, in red, e.g.: Abandon.", SECOND to ArgDto.text("Abandon"), LAST_IS_DANGER to ArgDto.boolean(true)),
                    story(
                        "three-actions",
                        "Three actions",
                        "Three buttons share it in thirds, e.g.: the merchant's choices.",
                        FIRST to ArgDto.text("Buy"),
                        SECOND to ArgDto.text("Sell"),
                        THIRD to ArgDto.text("Leave"),
                    ),
                    story(
                        "disabled-action",
                        "Disabled action",
                        "An action that can not be done, faded, reading why, e.g.: in the shop.",
                        SECOND to ArgDto.text("Not enough gold"),
                        LAST_IS_ENABLED to ArgDto.boolean(false),
                    ),
                ),
        )

    override fun render(
        args: Args,
        context: StoryContext,
    ): View {
        val labels = listOf(args.text(FIRST), args.text(SECOND), args.text(THIRD)).filter { label -> label.isNotEmpty() }
        return ActionRowView(
            labels.mapIndexed { index, label ->
                val isLast = index == labels.lastIndex
                val onSelected = { context.action("onSelected", label) }
                if (isLast && args.boolean(LAST_IS_DANGER)) {
                    ActionRowView.Action(label, HudStyle.DANGER, HudStyle.DANGER_PRESSED, onSelected = onSelected)
                } else {
                    ActionRowView.Action(label, isEnabled = !isLast || args.boolean(LAST_IS_ENABLED), onSelected = onSelected)
                }
            },
        )
    }
}
