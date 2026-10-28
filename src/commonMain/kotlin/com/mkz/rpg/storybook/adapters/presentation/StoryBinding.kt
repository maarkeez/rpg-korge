package com.mkz.rpg.storybook.adapters.presentation

import com.mkz.rpg.storybook.domain.Storybook.Dto.ComponentDto
import korlibs.korge.view.View

/**
 * Binds a component of the catalogue to the real view of the game it shows, one per view, named after it (e.g.:
 * [HealthBarViewStories] for `HealthBarView`). Each one is listed in [StorybookApi.STORY_BINDINGS].
 */
interface StoryBinding {
    /**
     * The component, with its controls, its default args and its stories, in the order they are listed. The options of
     * its controls may come from the [context], e.g.: the lootable item ids from the images loaded.
     */
    fun component(context: StoryContext): ComponentDto

    /** A new view, displaying the [args], one per control of the component. */
    fun render(
        args: Args,
        context: StoryContext,
    ): View
}
