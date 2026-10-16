package com.mkz.rpg.mapBuilder.usecases.commands

import com.mkz.rpg.mapBuilder.domain.MapBuilderError.MapNotCreated
import com.mkz.rpg.mapBuilder.domain.MapBuilderRepository
import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.terrain.usecases.queries.IsTransitionAllowed

/**
 * Paints the tile with the selected terrain. Two different terrains can only be neighbours when there is a transition
 * between them, in either direction, as the battlefield requires.
 */
class PaintTile(
    private val mapBuilderRepository: MapBuilderRepository,
    private val isTransitionAllowed: IsTransitionAllowed,
    private val eventBus: EventBus,
) {
    operator fun invoke(
        row: Int,
        column: Int,
    ) {
        val mapBuilder = mapBuilderRepository.search() ?: throw MapNotCreated()
        val (events, paintedMapBuilder) =
            mapBuilder
                .paint(row = row, column = column) { terrainId, neighbourTerrainId ->
                    isTransitionAllowed(terrainId, neighbourTerrainId) || isTransitionAllowed(neighbourTerrainId, terrainId)
                }.pullEvents()
        mapBuilderRepository.save(paintedMapBuilder)
        eventBus.publish(events)
    }
}
