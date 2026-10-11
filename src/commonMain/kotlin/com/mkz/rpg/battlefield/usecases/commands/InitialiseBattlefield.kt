package com.mkz.rpg.battlefield.usecases.commands

import com.mkz.rpg.battlefield.domain.BattlefieldRepository
import com.mkz.rpg.battlefield.usecases.services.BattlefieldCreation
import com.mkz.rpg.battlefield.usecases.services.BattlefieldLoader
import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.terrain.usecases.queries.SearchTerrainById

class InitialiseBattlefield(
    private val battlefieldLoader: BattlefieldLoader,
    battlefieldRepository: BattlefieldRepository,
    searchTerrainById: SearchTerrainById,
    private val eventBus: EventBus,
) {
    private val battlefieldCreation = BattlefieldCreation(battlefieldRepository, searchTerrainById)

    operator fun invoke() {
        val tiles = battlefieldLoader.loadTiles()
        eventBus.publish(battlefieldCreation.create(rows = tiles.size, columns = tiles.firstOrNull()?.size ?: 0, tiles = tiles))
    }
}
