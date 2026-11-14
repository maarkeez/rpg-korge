package com.mkz.tessavar.lootDrop.usecases.commands

import com.mkz.tessavar.lootDrop.domain.LootDrop.Dto.PositionDto
import com.mkz.tessavar.lootDrop.domain.LootDropEvent.DroppedItemsFound
import com.mkz.tessavar.lootDrop.domain.LootDropRepository
import com.mkz.tessavar.shared.domain.EventBus

class FindDroppedItems(
    private val lootDropRepository: LootDropRepository,
    private val eventBus: EventBus,
) {
    operator fun invoke(
        battleUnitId: String,
        path: List<PositionDto>,
    ) {
        val lootDrops = lootDropRepository.searchAll()
        val droppedItemsFoundEvents =
            path.mapNotNull { position ->
                val lootableItemIds = lootDrops.flatMap { lootDrop -> lootDrop.droppedItems(position.row, position.column) }
                if (lootableItemIds.isEmpty()) return@mapNotNull null
                DroppedItemsFound(battleUnitId = battleUnitId, lootableItemIds = lootableItemIds, row = position.row, column = position.column)
            }
        droppedItemsFoundEvents.forEach(eventBus::publish)
    }
}
