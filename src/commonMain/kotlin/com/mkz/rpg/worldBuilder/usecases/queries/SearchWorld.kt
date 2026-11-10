package com.mkz.rpg.worldBuilder.usecases.queries

import com.mkz.rpg.worldBuilder.domain.WorldBuilder
import com.mkz.rpg.worldBuilder.domain.WorldBuilderRepository

class SearchWorld(
    private val worldBuilderRepository: WorldBuilderRepository,
) {
    operator fun invoke(): WorldBuilder.Dto? = worldBuilderRepository.search()?.toDto()
}
