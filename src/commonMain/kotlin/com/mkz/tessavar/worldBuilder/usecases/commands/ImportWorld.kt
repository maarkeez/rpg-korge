package com.mkz.tessavar.worldBuilder.usecases.commands

import com.mkz.tessavar.battlefield.domain.BattlefieldError.InvalidBattlefieldFormat
import com.mkz.tessavar.battlefield.usecases.queries.ParseBattlefield
import com.mkz.tessavar.shared.domain.EventBus
import com.mkz.tessavar.terrain.usecases.queries.HasTerrainTransition
import com.mkz.tessavar.terrain.usecases.queries.SearchTerrainById
import com.mkz.tessavar.worldBuilder.domain.WorldBuilderError.WorldNotCreated
import com.mkz.tessavar.worldBuilder.domain.WorldBuilderRepository

class ImportWorld(
    private val worldBuilderRepository: WorldBuilderRepository,
    private val parseBattlefield: ParseBattlefield,
    private val searchTerrainById: SearchTerrainById,
    private val hasTerrainTransition: HasTerrainTransition,
    private val eventBus: EventBus,
) {
    operator fun invoke(text: String) {
        val worldBuilder = worldBuilderRepository.search() ?: throw WorldNotCreated()
        val tiles =
            try {
                parseBattlefield(text)
            } catch (_: InvalidBattlefieldFormat) {
                null
            }
        val (events, importedWorldBuilder) =
            worldBuilder
                .import(
                    tiles = tiles,
                    isKnownTerrain = { terrainId -> searchTerrainById(terrainId) != null },
                    canBeAdjacent = { terrainId, adjacentTerrainId ->
                        hasTerrainTransition(terrainId, adjacentTerrainId) || hasTerrainTransition(adjacentTerrainId, terrainId)
                    },
                ).pullEvents()
        worldBuilderRepository.save(importedWorldBuilder)
        eventBus.publish(events)
    }
}
