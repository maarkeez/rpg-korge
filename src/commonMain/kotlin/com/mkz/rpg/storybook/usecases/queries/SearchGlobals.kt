package com.mkz.rpg.storybook.usecases.queries

import com.mkz.rpg.storybook.domain.Storybook.Dto.ArgDto
import com.mkz.rpg.storybook.domain.StorybookRepository

/** The globals every story is displayed with, e.g.: the language, or none while the catalogue is not initialised. */
class SearchGlobals(
    private val storybookRepository: StorybookRepository,
) {
    operator fun invoke(): Map<String, ArgDto> =
        storybookRepository
            .search()
            ?.toDto()
            ?.globals
            .orEmpty()
}
