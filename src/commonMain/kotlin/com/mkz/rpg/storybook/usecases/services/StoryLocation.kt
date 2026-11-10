package com.mkz.rpg.storybook.usecases.services

import com.mkz.rpg.storybook.domain.Storybook.Dto.LocationDto

interface StoryLocation {
    fun search(): LocationDto

    fun replace(location: LocationDto)
}
