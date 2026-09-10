package com.mkz.rpg.battlefield.usecases.commands

import com.mkz.rpg.battlefield.domain.BattlefieldRepository
import com.mkz.rpg.shared.domain.EventBus

class RemoveOccupant(
    private val battlefieldRepository: BattlefieldRepository,
    private val eventBus: EventBus,
) {
    operator fun invoke(battleUnitId: String) {
        val storedBattlefield = battlefieldRepository.search() ?: return
        val (events, battlefield) = storedBattlefield.removeOccupant(battleUnitId).pullEvents()
        battlefieldRepository.update(battlefield)
        eventBus.publish(events)
    }
}
