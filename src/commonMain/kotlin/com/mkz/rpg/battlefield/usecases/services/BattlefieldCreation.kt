package com.mkz.rpg.battlefield.usecases.services

import com.mkz.rpg.battlefield.domain.Battlefield
import com.mkz.rpg.battlefield.domain.Battlefield.Dto.TerrainTransitionRuleDto
import com.mkz.rpg.battlefield.domain.BattlefieldEvent
import com.mkz.rpg.battlefield.domain.BattlefieldRepository
import com.mkz.rpg.terrain.usecases.queries.SearchTerrainById

/**
 * Creates the battlefield, unless it already exists, from the terrain id of each tile. Returns the events to publish.
 * Two terrains without a transition between them can not be next to each other.
 */
class BattlefieldCreation(
    private val battlefieldRepository: BattlefieldRepository,
    private val searchTerrainById: SearchTerrainById,
) {
    fun create(
        rows: Int,
        columns: Int,
        tiles: List<List<String>>,
    ): Set<BattlefieldEvent> {
        if (battlefieldRepository.search() != null) return emptySet()
        val (events, battlefield) = build(rows = rows, columns = columns, tiles = tiles).pullEvents()
        battlefieldRepository.create(battlefield)
        return events
    }

    /** Builds the battlefield the same way, without storing it, e.g.: to display a map being designed. */
    fun preview(tiles: List<List<String>>): Battlefield.Dto = build(rows = tiles.size, columns = tiles.firstOrNull()?.size ?: 0, tiles = tiles).toDto()

    private fun build(
        rows: Int,
        columns: Int,
        tiles: List<List<String>>,
    ): Battlefield =
        Battlefield.create(
            rows = rows,
            columns = columns,
            tiles = tiles,
            terrainCanBeOccupied = terrainOccupancy(tiles),
            terrainTransitionRules = terrainTransitionRules(tiles),
        )

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
