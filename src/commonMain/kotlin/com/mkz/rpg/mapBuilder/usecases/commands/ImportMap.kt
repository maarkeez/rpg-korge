package com.mkz.rpg.mapBuilder.usecases.commands

import com.mkz.rpg.battlefield.domain.BattlefieldError.InvalidBattlefieldFormat
import com.mkz.rpg.battlefield.usecases.queries.ParseBattlefield
import com.mkz.rpg.mapBuilder.domain.MapBuilderError.MapNotCreated
import com.mkz.rpg.mapBuilder.domain.MapBuilderRepository
import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.terrain.usecases.queries.IsTransitionAllowed
import com.mkz.rpg.terrain.usecases.queries.SearchTerrainById

/**
 * Replaces the map with the one written in the text, in the format the battlefield is loaded from (e.g.: an exported
 * map). Its terrains must exist, and two different terrains can only be neighbours when there is a transition between
 * them, in either direction, as the battlefield requires.
 */
class ImportMap(
    private val mapBuilderRepository: MapBuilderRepository,
    private val parseBattlefield: ParseBattlefield,
    private val searchTerrainById: SearchTerrainById,
    private val isTransitionAllowed: IsTransitionAllowed,
    private val eventBus: EventBus,
) {
    operator fun invoke(text: String) {
        val mapBuilder = mapBuilderRepository.search() ?: throw MapNotCreated()
        val tiles =
            try {
                parseBattlefield(text)
            } catch (_: InvalidBattlefieldFormat) {
                null
            }
        val (events, importedMapBuilder) =
            mapBuilder
                .import(
                    tiles = tiles,
                    isKnownTerrain = { terrainId -> searchTerrainById(terrainId) != null },
                    canBeNeighbours = { terrainId, neighbourTerrainId ->
                        isTransitionAllowed(terrainId, neighbourTerrainId) || isTransitionAllowed(neighbourTerrainId, terrainId)
                    },
                ).pullEvents()
        mapBuilderRepository.save(importedMapBuilder)
        eventBus.publish(events)
    }
}
