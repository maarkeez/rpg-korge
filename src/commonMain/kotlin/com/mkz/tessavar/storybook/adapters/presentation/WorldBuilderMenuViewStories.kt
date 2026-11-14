package com.mkz.tessavar.storybook.adapters.presentation

import com.mkz.tessavar.screen.adapters.presentation.UiMessage
import com.mkz.tessavar.screen.adapters.presentation.WorldBuilderMenuView
import com.mkz.tessavar.storybook.domain.Storybook.Dto.ComponentDto
import com.mkz.tessavar.storybook.domain.Storybook.Dto.ControlDto
import com.mkz.tessavar.storybook.domain.Storybook.Dto.StoryDto
import com.mkz.tessavar.storybook.domain.Storybook.Dto.ValueDto
import korlibs.korge.view.FixedSizeContainer
import korlibs.korge.view.View
import korlibs.korge.view.addUpdater
import korlibs.math.geom.Size

object WorldBuilderMenuViewStories : StoryBinding {
    private const val OPTIONS_LIMIT = 6
    private val BUTTON_SIZE = Size(WorldBuilderMenuView.BUTTON_WIDTH, WorldBuilderMenuView.BUTTON_HEIGHT)

    private const val OPEN = "open"
    private const val OPTIONS = "options"

    private val closed = StoryDto(id = "screen-world-builder-menu--closed", name = "Closed", description = "The hamburger button: tapping it opens the options.", args = emptyMap())
    private val open =
        StoryDto(
            id = "screen-world-builder-menu--open",
            name = "Open",
            description = "The options below the button, as the world builder lists them: Import.",
            args =
                mapOf(
                    OPEN to ValueDto.boolean(true),
                ),
        )
    private val severalOptions =
        StoryDto(
            id = "screen-world-builder-menu--several-options",
            name = "Several options",
            description = "More options than the world builder has, one under the other.",
            args = mapOf(OPEN to ValueDto.boolean(true), OPTIONS to ValueDto.int(4)),
        )

    override fun component(context: StoryContext) =
        ComponentDto(
            id = "screen-world-builder-menu",
            group = "Screen",
            title = "World builder menu",
            description = "At the top right of the world builder: a hamburger button which opens a list of options below it, over the screen. open: whether the options are listed. options: how many.",
            controls = listOf(ControlDto.boolean(OPEN), ControlDto.int(OPTIONS, minimum = 1, maximum = OPTIONS_LIMIT)),
            args = mapOf(OPEN to ValueDto.boolean(false), OPTIONS to ValueDto.int(1)),
            stories = listOf(closed, open, severalOptions),
        )

    override fun render(
        args: Args,
        context: StoryContext,
    ): View {
        val labels = listOf(context.messages[UiMessage.WORLD_BUILDER_IMPORT]) + (2..args.int(OPTIONS)).map { number -> "Option $number" }
        val menuView =
            WorldBuilderMenuView().apply {
                displayOptions(
                    labels.map { label ->
                        WorldBuilderMenuView.Option(label = label) { context.action("onSelected", label, setArg = OPEN to ValueDto.boolean(false)) }
                    },
                )
                if (args.boolean(OPEN)) {
                    var opened = false
                    addUpdater {
                        if (!opened) {
                            opened = true
                            open()
                        }
                    }
                }
            }
        return FixedSizeContainer(BUTTON_SIZE).apply { addChild(menuView) }
    }
}
