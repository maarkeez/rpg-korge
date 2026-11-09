package com.mkz.rpg.worldBuilder.usecases.commands

import com.mkz.rpg.battlefield.domain.BattlefieldError.InvalidBattlefieldFormat
import com.mkz.rpg.battlefield.usecases.queries.ParseBattlefield
import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.terrain.usecases.queries.HasTerrainTransition
import com.mkz.rpg.terrain.usecases.queries.SearchTerrainById
import com.mkz.rpg.worldBuilder.domain.WorldBuilderError.WorldNotCreated
import com.mkz.rpg.worldBuilder.domain.WorldBuilderRepository

/**
 * Replaces the world with the one written in the text, in the format the battlefield is loaded from (e.g.: an exported
 * world). Its terrains must exist, and two different terrains can only be adjacent when there is a transition between
 * them, in either direction, as the battlefield requires.
 */
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
