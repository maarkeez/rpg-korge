package com.mkz.rpg.worldBuilder.usecases.queries

import com.mkz.rpg.worldBuilder.domain.WorldBuilder
import com.mkz.rpg.worldBuilder.domain.WorldBuilderRepository

/** The world being built, or `null` before it is created. */
class SearchWorld(
    private val worldBuilderRepository: WorldBuilderRepository,
) {
    operator fun invoke(): WorldBuilder.Dto? = worldBuilderRepository.search()?.toDto()
}
