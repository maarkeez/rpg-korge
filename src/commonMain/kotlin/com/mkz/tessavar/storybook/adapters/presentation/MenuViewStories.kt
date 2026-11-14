package com.mkz.tessavar.storybook.adapters.presentation

import com.mkz.tessavar.screen.adapters.presentation.MenuView
import com.mkz.tessavar.storybook.domain.Storybook.Dto.ComponentDto
import com.mkz.tessavar.storybook.domain.Storybook.Dto.StoryDto
import korlibs.korge.view.View

object MenuViewStories : StoryBinding {
    private val options =
        StoryDto(
            id = "screen-menu--options",
            name = "Options",
            description = "Play new game, Load game, Build world, Language, Privacy and Wishlist in gold. Language lists the languages, Privacy opens the consent panel, Wishlist the signup form.",
            args = emptyMap(),
        )

    override fun component(context: StoryContext) =
        ComponentDto(
            id = "screen-menu",
            group = "Screen",
            title = "Menu",
            description = "The first screen of the game: playing a new game, its primary action, loading a game, building a world, the language, the privacy choice, or joining the mailing list.",
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
            onWishlistSelected = { context.action("onWishlistSelected") },
        )
}
