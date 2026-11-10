package com.mkz.rpg.storybook.usecases.queries

import com.mkz.rpg.storybook.domain.Storybook.Dto.ValueDto
import com.mkz.rpg.storybook.domain.StorybookRepository

class SearchArgs(
    private val storybookRepository: StorybookRepository,
) {
    operator fun invoke(): Map<String, ValueDto> =
        storybookRepository
            .search()
            ?.toDto()
            ?.args
            .orEmpty()
}
