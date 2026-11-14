package com.mkz.tessavar.storybook.adapters.location

import com.mkz.tessavar.storybook.usecases.services.StoryLocation

expect fun isStorybookRequested(): Boolean

expect fun platformStoryLocation(): StoryLocation
