package com.mkz.rpg.storybook.adapters.location

import com.mkz.rpg.storybook.usecases.services.StoryLocation

/** The system property choosing what starts, e.g.: `-Drpg.entry=storybook`, set by `runJvmStorybook`. */
private const val ENTRY_PROPERTY = "rpg.entry"
private const val STORYBOOK_ENTRY = "storybook"

actual fun isStorybookRequested(): Boolean = System.getProperty(ENTRY_PROPERTY) == STORYBOOK_ENTRY

actual fun platformStoryLocation(): StoryLocation = NoStoryLocation()
