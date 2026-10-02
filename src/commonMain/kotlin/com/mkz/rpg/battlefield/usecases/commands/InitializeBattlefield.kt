package com.mkz.rpg.battlefield.usecases.commands

import com.mkz.rpg.battlefield.domain.Battlefield
import com.mkz.rpg.battlefield.domain.Battlefield.Dto.TerrainTransitionRuleDto
import com.mkz.rpg.battlefield.domain.BattlefieldRepository
import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.terrain.usecases.queries.SearchTerrainById

class InitializeBattlefield(
    private val battlefieldRepository: BattlefieldRepository,
    private val searchTerrainById: SearchTerrainById,
    private val eventBus: EventBus,
) {
    operator fun invoke(
        rows: Int,
        columns: Int,
        tiles: List<List<String>>,
    ) {
        if (battlefieldRepository.search() != null) return
        val (events, battlefield) =
            Battlefield
                .create(
                    rows = rows,
                    columns = columns,
                    tiles = tiles,
                    terrainCanBeOccupied = terrainOccupancy(tiles),
                    terrainTransitionRules = terrainTransitionRules(tiles),
                ).pullEvents()
        battlefieldRepository.create(battlefield)
        eventBus.publish(events)
    }

    private fun terrainOccupancy(tiles: List<List<String>>): Map<String, Boolean> =
        tiles
            .flatten()
            .distinct()
            .associateWith { terrainId -> searchTerrainById(terrainId)?.canBeOccupied ?: false }

    private fun terrainTransitionRules(tiles: List<List<String>>): Set<TerrainTransitionRuleDto> =
        tiles
            .flatten()
            .distinct()
            .flatMap { terrainId ->
                (searchTerrainById(terrainId)?.allowedTransitionTo ?: emptySet())
                    .filter { it != terrainId }
                    .map { TerrainTransitionRuleDto(terrainId, it) }
            }.toSet()
}
