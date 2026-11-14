package com.mkz.tessavar.storybook.adapters.presentation

import com.mkz.tessavar.storybook.domain.Storybook.Dto.ComponentDto
import com.mkz.tessavar.storybook.domain.Storybook.Dto.StoryDto

class FakeStoryInfoView : StoryInfoView() {
    val displayedStories = mutableListOf<Pair<ComponentDto, StoryDto>>()

    override fun display(
        component: ComponentDto,
        story: StoryDto,
    ) {
        displayedStories += component to story
    }
}
