package com.mkz.tessavar.storybook.adapters.location

import com.mkz.tessavar.storybook.usecases.services.StoryLocation

private const val ENTRY_PROPERTY = "rpg.entry"
private const val STORYBOOK_ENTRY = "storybook"

actual fun isStorybookRequested(): Boolean = System.getProperty(ENTRY_PROPERTY) == STORYBOOK_ENTRY

actual fun platformStoryLocation(): StoryLocation = NoStoryLocation()
