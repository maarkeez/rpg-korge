package com.mkz.rpg.storybook.usecases.queries

import com.mkz.rpg.storybook.domain.Storybook.Dto.ValueDto
import com.mkz.rpg.storybook.domain.StorybookRepository

/** The globals every story is displayed with, e.g.: the language, or none while the catalogue is not open. */
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
