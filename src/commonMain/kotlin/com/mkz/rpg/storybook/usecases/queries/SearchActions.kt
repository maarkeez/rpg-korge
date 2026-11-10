package com.mkz.rpg.storybook.usecases.queries

import com.mkz.rpg.storybook.domain.Storybook.Dto.ActionDto
import com.mkz.rpg.storybook.domain.StorybookRepository

class SearchActions(
    private val storybookRepository: StorybookRepository,
) {
    operator fun invoke(): List<ActionDto> =
        storybookRepository
            .search()
            ?.toDto()
            ?.actions
            .orEmpty()
}
