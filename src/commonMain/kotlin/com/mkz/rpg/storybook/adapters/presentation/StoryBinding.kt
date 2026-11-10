package com.mkz.rpg.storybook.adapters.presentation

import com.mkz.rpg.storybook.domain.Storybook.Dto.ComponentDto
import korlibs.korge.view.View

interface StoryBinding {
    fun component(context: StoryContext): ComponentDto

    fun render(
        args: Args,
        context: StoryContext,
    ): View
}
