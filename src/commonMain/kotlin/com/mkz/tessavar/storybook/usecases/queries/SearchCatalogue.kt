package com.mkz.tessavar.storybook.usecases.queries

import com.mkz.tessavar.storybook.domain.Storybook.Dto.ComponentDto
import com.mkz.tessavar.storybook.domain.StorybookRepository

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
