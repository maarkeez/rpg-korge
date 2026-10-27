package com.mkz.rpg.storybook.adapters.presentation

import com.mkz.rpg.storybook.domain.Storybook.Dto.ComponentDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.StoryDto

// Records the stories the presenter asks to describe, instead of drawing them.
class FakeStoryInfoView : StoryInfoView() {
    val displayedStories = mutableListOf<Pair<ComponentDto, StoryDto>>()

    override fun display(
        component: ComponentDto,
        story: StoryDto,
    ) {
        displayedStories += component to story
    }
}
