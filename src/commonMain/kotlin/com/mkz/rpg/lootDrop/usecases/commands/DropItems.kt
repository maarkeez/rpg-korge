package com.mkz.rpg.lootDrop.usecases.commands

import com.mkz.rpg.lootDrop.domain.LootDrop
import com.mkz.rpg.lootDrop.domain.LootDropRepository
import com.mkz.rpg.shared.domain.EventBus

class DropItems(
    private val lootDropRepository: LootDropRepository,
    private val eventBus: EventBus,
) {
    operator fun invoke(
        lootableItemIds: List<String>,
        row: Int,
        column: Int,
    ) {
        lootableItemIds.forEach { lootableItemId ->
            val storedLootDrop = requireNotNull(lootDropRepository.searchByUnitId(LootDrop.OWNERLESS_UNIT_ID)) { "The loot drops are not initialised" }
            val (events, lootDrop) = storedLootDrop.drop(lootableItemId, row, column).pullEvents()
            lootDropRepository.update(lootDrop)
            eventBus.publish(events)
        }
    }
}
