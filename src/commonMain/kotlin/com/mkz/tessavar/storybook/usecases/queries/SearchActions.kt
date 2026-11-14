package com.mkz.tessavar.storybook.usecases.queries

import com.mkz.tessavar.storybook.domain.Storybook.Dto.ActionDto
import com.mkz.tessavar.storybook.domain.StorybookRepository

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
