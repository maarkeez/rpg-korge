package com.mkz.rpg.storybook.adapters.location

import com.mkz.rpg.storybook.usecases.services.StoryLocation

/** Whether the Storybook is asked for instead of the game: by the page on JS, and by a system property on the JVM. */
expect fun isStorybookRequested(): Boolean

/** Where the story on show is kept on this platform. */
expect fun platformStoryLocation(): StoryLocation
