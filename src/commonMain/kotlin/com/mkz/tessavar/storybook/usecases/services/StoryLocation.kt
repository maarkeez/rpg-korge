package com.mkz.tessavar.storybook.usecases.services

import com.mkz.tessavar.storybook.domain.Storybook.Dto.LocationDto

interface StoryLocation {
    fun search(): LocationDto

    fun replace(location: LocationDto)
}
