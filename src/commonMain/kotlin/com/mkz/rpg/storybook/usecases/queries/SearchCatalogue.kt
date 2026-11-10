package com.mkz.rpg.storybook.usecases.queries

import com.mkz.rpg.storybook.domain.Storybook.Dto.ComponentDto
import com.mkz.rpg.storybook.domain.StorybookRepository

class SearchCatalogue(
    private val storybookRepository: StorybookRepository,
) {
    operator fun invoke(): List<ComponentDto> =
        storybookRepository
            .search()
            ?.toDto()
            ?.components
            .orEmpty()
}
