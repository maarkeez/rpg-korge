package com.mkz.rpg.storybook.adapters.presentation

import com.mkz.rpg.screen.adapters.presentation.PauseView
import com.mkz.rpg.storybook.domain.Storybook.Dto.ComponentDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.StoryDto
import korlibs.korge.view.View

/** The stories of [PauseView], built with its real buttons. Tapping an option logs it. */
object PauseViewStories : StoryBinding {
    private val options =
        StoryDto(
            id = "screen-pause--options",
            name = "Options",
            description = "Resume, its primary action, Save and Quit to menu, one under the other.",
            args = emptyMap(),
        )

    override fun component(context: StoryContext) =
        ComponentDto(
            id = "screen-pause",
            group = "Screen",
            title = "Pause",
            description = "Where the pad is, while the game is paused with Start: time stops until Resume.",
            controls = emptyList(),
            args = emptyMap(),
            stories = listOf(options),
        )

    override fun render(
        args: Args,
        context: StoryContext,
    ): View =
        PauseView(context.texts).apply {
            setDelegate(
                object : PauseView.Delegate {
                    override fun pauseResumeSelected() = context.action("pauseResumeSelected")

                    override fun pauseSaveSelected() = context.action("pauseSaveSelected")

                    override fun pauseQuitSelected() = context.action("pauseQuitSelected")
                },
            )
        }
}
