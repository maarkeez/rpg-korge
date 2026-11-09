package com.mkz.rpg.storybook.adapters.presentation

import com.mkz.rpg.screen.adapters.presentation.UiMessage
import com.mkz.rpg.screen.adapters.presentation.WorldBuilderMenuView
import com.mkz.rpg.storybook.domain.Storybook.Dto.ArgDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.ComponentDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.ControlDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.StoryDto
import korlibs.korge.view.FixedSizeContainer
import korlibs.korge.view.View
import korlibs.korge.view.addUpdater
import korlibs.math.geom.Size

/**
 * The stories of [WorldBuilderMenuView], with the messages of the language global. As in the game, choosing an option closes
 * the menu, through its args. The menu is shown in the middle of the phone instead of its top right corner, so the
 * taps out of it, which close it in the game, do not reach the whole screen.
 */
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
                    OPEN to ArgDto.boolean(true),
                ),
        )
    private val severalOptions =
        StoryDto(
            id = "screen-world-builder-menu--several-options",
            name = "Several options",
            description = "More options than the world builder has, one under the other.",
            args = mapOf(OPEN to ArgDto.boolean(true), OPTIONS to ArgDto.int(4)),
        )

    override fun component(context: StoryContext) =
        ComponentDto(
            id = "screen-world-builder-menu",
            group = "Screen",
            title = "World builder menu",
            description = "At the top right of the world builder: a hamburger button which opens a list of options below it, over the screen. open: whether the options are listed. options: how many.",
            controls = listOf(ControlDto.boolean(OPEN), ControlDto.int(OPTIONS, minimum = 1, maximum = OPTIONS_LIMIT)),
            args = mapOf(OPEN to ArgDto.boolean(false), OPTIONS to ArgDto.int(1)),
            stories = listOf(closed, open, severalOptions),
        )

    override fun render(
        args: Args,
        context: StoryContext,
    ): View {
        // The first option is the one of the world builder, the others are there to see a longer list.
        val labels = listOf(context.messages[UiMessage.WORLD_BUILDER_IMPORT]) + (2..args.int(OPTIONS)).map { number -> "Option $number" }
        val menuView =
            WorldBuilderMenuView().apply {
                displayOptions(
                    labels.map { label ->
                        WorldBuilderMenuView.Option(label = label) { context.action("onSelected", label, setArg = OPEN to ArgDto.boolean(false)) }
                    },
                )
                // Opened once on the phone, as the menu measures where it is on the screen to catch the taps out of it.
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
        // As big as the button, so the phone centers the button, and the options open around it.
        return FixedSizeContainer(BUTTON_SIZE).apply { addChild(menuView) }
    }
}
