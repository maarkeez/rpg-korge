package com.mkz.rpg.storybook.usecases.queries

import com.mkz.rpg.storybook.domain.Storybook.Dto.ComponentDto
import com.mkz.rpg.storybook.domain.StorybookRepository

/** The components, in the order they are listed, or none while the catalogue is not initialised. */
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
