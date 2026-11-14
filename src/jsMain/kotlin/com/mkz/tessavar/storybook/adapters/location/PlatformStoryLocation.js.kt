package com.mkz.tessavar.storybook.adapters.location

import com.mkz.tessavar.storybook.usecases.services.StoryLocation
import kotlinx.browser.window

private const val STORYBOOK_FOLDER = "/storybook"

actual fun isStorybookRequested(): Boolean =
    window.location.pathname
        .substringBeforeLast("/")
        .endsWith(STORYBOOK_FOLDER)

actual fun platformStoryLocation(): StoryLocation = BrowserStoryLocation()
