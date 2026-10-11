package com.mkz.rpg.battlefield.usecases.commands

import com.mkz.rpg.battlefield.domain.BattlefieldRepository
import com.mkz.rpg.battlefield.usecases.services.BattlefieldCreation
import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.terrain.usecases.queries.SearchTerrainById

class RequestBattlefieldCreation(
    battlefieldRepository: BattlefieldRepository,
    searchTerrainById: SearchTerrainById,
    private val eventBus: EventBus,
) {
    private val battlefieldCreation = BattlefieldCreation(battlefieldRepository, searchTerrainById)

    operator fun invoke(
        rows: Int,
        columns: Int,
        tiles: List<List<String>>,
    ) {
        eventBus.publish(battlefieldCreation.create(rows = rows, columns = columns, tiles = tiles))
    }
}
