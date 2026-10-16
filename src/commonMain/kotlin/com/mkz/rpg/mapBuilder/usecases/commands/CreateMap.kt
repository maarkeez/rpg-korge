package com.mkz.rpg.mapBuilder.usecases.commands

import com.mkz.rpg.mapBuilder.domain.MapBuilder
import com.mkz.rpg.mapBuilder.domain.MapBuilderRepository
import com.mkz.rpg.shared.domain.EventBus

/** Starts designing a new map, replacing the previous one. */
class CreateMap(
    private val mapBuilderRepository: MapBuilderRepository,
    private val eventBus: EventBus,
) {
    operator fun invoke() {
        val (events, mapBuilder) = MapBuilder.create().pullEvents()
        mapBuilderRepository.save(mapBuilder)
        eventBus.publish(events)
    }
}
