package com.mkz.rpg.battlefield.usecases.commands

import com.mkz.rpg.battlefield.domain.BattlefieldRepository
import com.mkz.rpg.shared.domain.EventBus

class UpdateBattlefieldOccupancy(
    private val battlefieldRepository: BattlefieldRepository,
    private val eventBus: EventBus,
) {
    operator fun invoke(
        row: Int,
        column: Int,
        battleUnitId: String,
    ) {
        val storedBattlefield = battlefieldRepository.search() ?: return
        val (events, battlefield) = storedBattlefield.occupy(row, column, battleUnitId).pullEvents()
        battlefieldRepository.update(battlefield)
        eventBus.publish(events)
    }
}
