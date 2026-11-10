package com.mkz.rpg.worldBuilder.usecases.commands

import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.terrain.usecases.queries.HasTerrainTransition
import com.mkz.rpg.worldBuilder.domain.WorldBuilderError.WorldNotCreated
import com.mkz.rpg.worldBuilder.domain.WorldBuilderRepository

class PaintTile(
    private val worldBuilderRepository: WorldBuilderRepository,
    private val hasTerrainTransition: HasTerrainTransition,
    private val eventBus: EventBus,
) {
    operator fun invoke(
        row: Int,
        column: Int,
    ) {
        val worldBuilder = worldBuilderRepository.search() ?: throw WorldNotCreated()
        val (events, paintedWorldBuilder) =
            worldBuilder
                .paint(row = row, column = column) { terrainId, adjacentTerrainId ->
                    hasTerrainTransition(terrainId, adjacentTerrainId) || hasTerrainTransition(adjacentTerrainId, terrainId)
                }.pullEvents()
        worldBuilderRepository.save(paintedWorldBuilder)
        eventBus.publish(events)
    }
}
