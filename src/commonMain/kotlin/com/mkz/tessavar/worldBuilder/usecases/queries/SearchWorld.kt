package com.mkz.tessavar.worldBuilder.usecases.queries

import com.mkz.tessavar.worldBuilder.domain.WorldBuilder
import com.mkz.tessavar.worldBuilder.domain.WorldBuilderRepository

class SearchWorld(
    private val worldBuilderRepository: WorldBuilderRepository,
) {
    operator fun invoke(): WorldBuilder.Dto? = worldBuilderRepository.search()?.toDto()
}
