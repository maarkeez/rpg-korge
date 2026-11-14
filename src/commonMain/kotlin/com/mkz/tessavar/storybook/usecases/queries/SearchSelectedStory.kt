package com.mkz.tessavar.storybook.usecases.queries

import com.mkz.tessavar.storybook.domain.Storybook.Dto.StoryDto
import com.mkz.tessavar.storybook.domain.StorybookRepository

class SearchSelectedStory(
    private val storybookRepository: StorybookRepository,
) {
    operator fun invoke(): StoryDto? {
        val storybook = storybookRepository.search()?.toDto() ?: return null
        return storybook.components.flatMap { component -> component.stories }.firstOrNull { story -> story.id == storybook.selectedStoryId }
    }
}
