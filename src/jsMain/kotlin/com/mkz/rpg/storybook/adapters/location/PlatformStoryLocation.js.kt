package com.mkz.rpg.storybook.adapters.location

import com.mkz.rpg.storybook.usecases.services.StoryLocation
import kotlinx.browser.window

/** The folder of the Storybook page, published next to the game, e.g.: `https://maarkeez.github.io/rpg-korge/storybook/`. */
private const val STORYBOOK_FOLDER = "/storybook"

actual fun isStorybookRequested(): Boolean =
    window.location.pathname
        .substringBeforeLast("/")
        .endsWith(STORYBOOK_FOLDER)

actual fun platformStoryLocation(): StoryLocation = NoStoryLocation()
