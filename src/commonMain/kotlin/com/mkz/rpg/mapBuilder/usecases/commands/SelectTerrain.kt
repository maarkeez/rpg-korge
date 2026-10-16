package com.mkz.rpg.mapBuilder.usecases.commands

import com.mkz.rpg.mapBuilder.domain.MapBuilderError.MapNotCreated
import com.mkz.rpg.mapBuilder.domain.MapBuilderRepository
import com.mkz.rpg.shared.domain.EventBus

class SelectTerrain(
    private val mapBuilderRepository: MapBuilderRepository,
    private val eventBus: EventBus,
) {
    operator fun invoke(terrainId: String) {
        val mapBuilder = mapBuilderRepository.search() ?: throw MapNotCreated()
        val (events, updatedMapBuilder) = mapBuilder.selectTerrain(terrainId).pullEvents()
        mapBuilderRepository.save(updatedMapBuilder)
        eventBus.publish(events)
    }
}
