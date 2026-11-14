package com.mkz.tessavar.lootDrop.usecases.commands

import com.mkz.tessavar.lootDrop.domain.LootDropRepository
import com.mkz.tessavar.shared.domain.EventBus

class DropItemWithoutRolling(
    private val lootDropRepository: LootDropRepository,
    private val eventBus: EventBus,
) {
    operator fun invoke(
        unitId: String,
        lootableItemId: String,
        row: Int,
        column: Int,
    ) {
        val storedLootDrop = lootDropRepository.searchByUnitId(unitId) ?: return
        val (events, lootDrop) = storedLootDrop.drop(lootableItemId, row, column).pullEvents()
        lootDropRepository.update(lootDrop)
        eventBus.publish(events)
    }
}
