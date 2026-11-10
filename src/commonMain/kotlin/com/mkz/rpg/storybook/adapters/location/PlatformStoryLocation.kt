package com.mkz.rpg.storybook.adapters.location

import com.mkz.rpg.storybook.usecases.services.StoryLocation

expect fun isStorybookRequested(): Boolean

expect fun platformStoryLocation(): StoryLocation
