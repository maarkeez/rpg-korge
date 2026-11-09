package com.mkz.rpg.storybook.usecases.queries

import com.mkz.rpg.storybook.domain.Storybook.Dto.ValueDto
import com.mkz.rpg.storybook.domain.StorybookRepository

/** The args the selected story is displayed with, one per control of its component, or none while the catalogue is not open. */
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
