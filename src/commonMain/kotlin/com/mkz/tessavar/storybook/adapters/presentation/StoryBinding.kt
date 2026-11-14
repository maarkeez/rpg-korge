package com.mkz.tessavar.storybook.adapters.presentation

import com.mkz.tessavar.storybook.domain.Storybook.Dto.ComponentDto
import korlibs.korge.view.View

interface StoryBinding {
    fun component(context: StoryContext): ComponentDto

    fun render(
        args: Args,
        context: StoryContext,
    ): View
}
