package com.mkz.rpg.storybook.usecases.queries

import com.mkz.rpg.storybook.domain.Storybook.Dto.StoryDto
import com.mkz.rpg.storybook.domain.StorybookRepository

/** The story on show, or `null` while the catalogue is not initialised. */
class SearchSelectedStory(
    private val storybookRepository: StorybookRepository,
) {
    operator fun invoke(): StoryDto? {
        val storybook = storybookRepository.search()?.toDto() ?: return null
        return storybook.components.flatMap { component -> component.stories }.firstOrNull { story -> story.id == storybook.selectedStoryId }
    }
}
