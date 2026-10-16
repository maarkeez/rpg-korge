package com.mkz.rpg.battlefield.usecases.queries

import com.mkz.rpg.battlefield.domain.Battlefield
import com.mkz.rpg.battlefield.domain.BattlefieldRepository
import com.mkz.rpg.battlefield.usecases.services.BattlefieldCreation
import com.mkz.rpg.terrain.usecases.queries.SearchTerrainById

/**
 * The battlefield the terrain id of each tile would make, with the terrain transitions and the occupancy of the battle,
 * without storing it. E.g.: to display a map being designed. Throws `TileTerrainTransitionNotAllowed` when two
 * terrains without a transition between them are next to each other.
 */
class PreviewBattlefield(
    battlefieldRepository: BattlefieldRepository,
    searchTerrainById: SearchTerrainById,
) {
    private val battlefieldCreation = BattlefieldCreation(battlefieldRepository, searchTerrainById)

    operator fun invoke(tiles: List<List<String>>): Battlefield.Dto = battlefieldCreation.preview(tiles)
}
