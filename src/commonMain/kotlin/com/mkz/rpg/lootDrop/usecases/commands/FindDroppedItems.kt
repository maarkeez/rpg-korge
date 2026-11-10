package com.mkz.rpg.lootDrop.usecases.commands

import com.mkz.rpg.lootDrop.domain.LootDrop.Dto.PositionDto
import com.mkz.rpg.lootDrop.domain.LootDropEvent.DroppedItemsFound
import com.mkz.rpg.lootDrop.domain.LootDropRepository
import com.mkz.rpg.shared.domain.EventBus

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
