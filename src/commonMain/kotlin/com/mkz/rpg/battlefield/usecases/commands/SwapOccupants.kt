package com.mkz.rpg.battlefield.usecases.commands

import com.mkz.rpg.battlefield.domain.BattlefieldRepository
import com.mkz.rpg.shared.domain.EventBus

class SwapOccupants(
    private val battlefieldRepository: BattlefieldRepository,
    private val eventBus: EventBus,
) {
    operator fun invoke(
        occupantId: String,
        otherOccupantId: String,
    ) {
        val storedBattlefield = battlefieldRepository.search() ?: return
        val (events, battlefield) = storedBattlefield.swapOccupants(occupantId, otherOccupantId).pullEvents()
        battlefieldRepository.update(battlefield)
        eventBus.publish(events)
    }
}
