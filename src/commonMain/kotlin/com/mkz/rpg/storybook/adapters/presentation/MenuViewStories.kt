package com.mkz.rpg.storybook.adapters.presentation

import com.mkz.rpg.screen.adapters.presentation.MenuView
import com.mkz.rpg.storybook.domain.Storybook.Dto.ComponentDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.StoryDto
import korlibs.korge.view.View

/**
 * The stories of [MenuView], built with its real buttons. As in the game, playing or building a map hides the options,
 * and picking a language translates the texts: both last until the story is shown again, e.g.: with Reset.
 */
object MenuViewStories : StoryBinding {
    private val options =
        StoryDto(
            id = "screen-menu--options",
            name = "Options",
            description = "Play new game, Load game, Build map and Language. Tapping Language lists the languages in place of the options.",
            args = emptyMap(),
        )

    override fun component(context: StoryContext) =
        ComponentDto(
            id = "screen-menu",
            group = "Screen",
            title = "Menu",
            description = "The first screen of the game: playing a new game, its primary action, loading a saved game, building a map, or picking the language.",
            controls = emptyList(),
            args = emptyMap(),
            stories = listOf(options),
        )

    override fun render(
        args: Args,
        context: StoryContext,
    ): View =
        MenuView(
            context.texts,
            onNewGameSelected = { context.action("onNewGameSelected") },
            onLoadGameSelected = { context.action("onLoadGameSelected") },
            onMapBuilderSelected = { context.action("onMapBuilderSelected") },
        )
}
