package com.mkz.tessavar.storybook.usecases.queries

import com.mkz.tessavar.storybook.domain.Storybook.Dto.ValueDto
import com.mkz.tessavar.storybook.domain.StorybookRepository

class SearchGlobals(
    private val storybookRepository: StorybookRepository,
) {
    operator fun invoke(): Map<String, ValueDto> =
        storybookRepository
            .search()
            ?.toDto()
            ?.globals
            .orEmpty()
}
