package com.mkz.rpg.storybook.adapters.presentation

import com.mkz.rpg.screen.adapters.presentation.MenuView
import com.mkz.rpg.storybook.domain.Storybook.Dto.ComponentDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.StoryDto
import korlibs.korge.view.View

object MenuViewStories : StoryBinding {
    private val options =
        StoryDto(
            id = "screen-menu--options",
            name = "Options",
            description = "Play new game, Load game, Build world, Language and Privacy. Tapping Language lists the languages in place of the options; Privacy opens the consent panel.",
            args = emptyMap(),
        )

    override fun component(context: StoryContext) =
        ComponentDto(
            id = "screen-menu",
            group = "Screen",
            title = "Menu",
            description = "The first screen of the game: playing a new game, its primary action, loading a saved game, building a world, picking the language, or the privacy choice.",
            controls = emptyList(),
            args = emptyMap(),
            stories = listOf(options),
        )

    override fun render(
        args: Args,
        context: StoryContext,
    ): View =
        MenuView(
            context.messages,
            onNewGameSelected = { context.action("onNewGameSelected") },
            onLoadGameSelected = { context.action("onLoadGameSelected") },
            onWorldBuilderSelected = { context.action("onWorldBuilderSelected") },
            onPrivacySelected = { context.action("onPrivacySelected") },
        )
}
