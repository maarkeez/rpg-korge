package com.mkz.tessavar.battlefield.usecases.commands

import com.mkz.tessavar.battlefield.domain.BattlefieldRepository
import com.mkz.tessavar.shared.domain.EventBus

class OccupyTile(
    private val battlefieldRepository: BattlefieldRepository,
    private val eventBus: EventBus,
) {
    operator fun invoke(
        row: Int,
        column: Int,
        occupantId: String,
    ) {
        val storedBattlefield = battlefieldRepository.search() ?: return
        val (events, battlefield) = storedBattlefield.occupy(row, column, occupantId).pullEvents()
        battlefieldRepository.update(battlefield)
        eventBus.publish(events)
    }
}
