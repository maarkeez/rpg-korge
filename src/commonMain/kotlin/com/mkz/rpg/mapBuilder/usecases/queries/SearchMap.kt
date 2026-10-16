package com.mkz.rpg.mapBuilder.usecases.queries

import com.mkz.rpg.mapBuilder.domain.MapBuilder
import com.mkz.rpg.mapBuilder.domain.MapBuilderRepository

/** The map being designed, or `null` before it is created. */
class SearchMap(
    private val mapBuilderRepository: MapBuilderRepository,
) {
    operator fun invoke(): MapBuilder.Dto? = mapBuilderRepository.search()?.toDto()
}
