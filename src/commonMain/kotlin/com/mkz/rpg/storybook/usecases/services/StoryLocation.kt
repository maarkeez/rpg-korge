package com.mkz.rpg.storybook.usecases.services

import com.mkz.rpg.storybook.domain.Storybook.Dto.LocationDto

/**
 * Where what is on show is kept, so a link opens it, e.g.: the page URL
 * `?story=screen-health-bar--full&args=remaining:3&globals=language:es`.
 */
interface StoryLocation {
    /** The story, args and globals asked for, none when nothing is. */
    fun search(): LocationDto

    /** Keeps what is on show in place of what is kept, so going back does not step through every change. */
    fun replace(location: LocationDto)
}
