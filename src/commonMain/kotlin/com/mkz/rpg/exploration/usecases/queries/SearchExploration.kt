package com.mkz.rpg.exploration.usecases.queries

import com.mkz.rpg.exploration.domain.Exploration
import com.mkz.rpg.exploration.domain.ExplorationRepository

class SearchExploration(
    private val explorationRepository: ExplorationRepository,
) {
    operator fun invoke(): Exploration.Dto? = explorationRepository.search()?.toDto()
}
