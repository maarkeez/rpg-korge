package com.mkz.rpg.battlefield.usecases.commands

import com.mkz.rpg.battlefield.domain.BattlefieldRepository
import com.mkz.rpg.shared.domain.EventBus

/** Places a dropped item on its tile, where it lies until an inventory stores it. */
class PlaceDroppedItem(
    private val battlefieldRepository: BattlefieldRepository,
    private val eventBus: EventBus,
) {
    operator fun invoke(
        lootableItemId: String,
        row: Int,
        column: Int,
    ) {
        val storedBattlefield = battlefieldRepository.search() ?: return
        val (events, battlefield) = storedBattlefield.placeDroppedItem(lootableItemId, row, column).pullEvents()
        battlefieldRepository.update(battlefield)
        eventBus.publish(events)
    }
}
