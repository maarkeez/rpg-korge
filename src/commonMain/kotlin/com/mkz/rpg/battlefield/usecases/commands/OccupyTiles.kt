package com.mkz.rpg.battlefield.usecases.commands

import com.mkz.rpg.battlefield.domain.BattlefieldRepository
import com.mkz.rpg.shared.domain.EventBus

/** Occupies every tile of the area at once, from its top-left tile, e.g.: a building placed on the battlefield. */
class OccupyTiles(
    private val battlefieldRepository: BattlefieldRepository,
    private val eventBus: EventBus,
) {
    operator fun invoke(
        row: Int,
        column: Int,
        rows: Int,
        columns: Int,
        occupantId: String,
    ) {
        val storedBattlefield = battlefieldRepository.search() ?: return
        val (events, battlefield) = storedBattlefield.occupyTiles(row, column, rows, columns, occupantId).pullEvents()
        battlefieldRepository.update(battlefield)
        eventBus.publish(events)
    }
}
