package com.mkz.rpg.storybook.usecases.queries

import com.mkz.rpg.storybook.domain.Storybook.Dto.ArgDto
import com.mkz.rpg.storybook.domain.StorybookRepository

/** The args the story on show is displayed with, one per control of its component, or none while the catalogue is not initialised. */
class SearchArgs(
    private val storybookRepository: StorybookRepository,
) {
    operator fun invoke(): Map<String, ArgDto> =
        storybookRepository
            .search()
            ?.toDto()
            ?.args
            .orEmpty()
}
