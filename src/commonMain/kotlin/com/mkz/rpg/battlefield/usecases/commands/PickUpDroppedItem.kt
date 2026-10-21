package com.mkz.rpg.battlefield.usecases.commands

import com.mkz.rpg.battlefield.domain.BattlefieldRepository
import com.mkz.rpg.shared.domain.EventBus

/** Removes a dropped item from its tile once an inventory has stored it, and requested its pick-up. */
class PickUpDroppedItem(
    private val battlefieldRepository: BattlefieldRepository,
    private val eventBus: EventBus,
) {
    operator fun invoke(
        lootableItemId: String,
        row: Int,
        column: Int,
    ) {
        val storedBattlefield = battlefieldRepository.search() ?: return
        val (events, battlefield) = storedBattlefield.pickUpDroppedItem(lootableItemId, row, column).pullEvents()
        battlefieldRepository.update(battlefield)
        eventBus.publish(events)
    }
}
