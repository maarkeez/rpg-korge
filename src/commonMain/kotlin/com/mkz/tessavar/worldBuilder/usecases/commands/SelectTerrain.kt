package com.mkz.tessavar.worldBuilder.usecases.commands

import com.mkz.tessavar.shared.domain.EventBus
import com.mkz.tessavar.worldBuilder.domain.WorldBuilderError.WorldNotCreated
import com.mkz.tessavar.worldBuilder.domain.WorldBuilderRepository

class SelectTerrain(
    private val worldBuilderRepository: WorldBuilderRepository,
    private val eventBus: EventBus,
) {
    operator fun invoke(terrainId: String) {
        val worldBuilder = worldBuilderRepository.search() ?: throw WorldNotCreated()
        val (events, updatedWorldBuilder) = worldBuilder.selectTerrain(terrainId).pullEvents()
        worldBuilderRepository.save(updatedWorldBuilder)
        eventBus.publish(events)
    }
}
