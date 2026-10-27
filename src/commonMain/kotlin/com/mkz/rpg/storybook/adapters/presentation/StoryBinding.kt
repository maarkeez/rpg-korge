package com.mkz.rpg.storybook.adapters.presentation

import com.mkz.rpg.storybook.domain.Storybook.Dto.ComponentDto
import korlibs.korge.view.View

/**
 * Binds a component of the catalogue to the real view of the game it shows, one per view, named after it (e.g.:
 * [HealthBarViewStories] for `HealthBarView`). Each one is listed in [StorybookApi.STORY_BINDINGS].
 */
interface StoryBinding {
    /** The component, with its stories, in the order they are listed. */
    val component: ComponentDto

    /** A new view, displaying the story, which must be one of the [component]. */
    fun render(
        storyId: String,
        context: StoryContext,
    ): View
}
