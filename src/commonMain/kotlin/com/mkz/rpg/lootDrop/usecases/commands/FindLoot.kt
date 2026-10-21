package com.mkz.rpg.lootDrop.usecases.commands

import com.mkz.rpg.lootDrop.domain.LootDrop.Dto.PositionDto
import com.mkz.rpg.lootDrop.domain.LootDropEvent.LootFound
import com.mkz.rpg.lootDrop.domain.LootDropRepository
import com.mkz.rpg.shared.domain.EventBus

/**
 * Announces the dropped items on each tile a battle unit walked or teleported onto, in walking order, so its inventory can store them. The items
 * of a tile are listed by the unit that dropped them, each in the order it dropped them. They stay on their tile until an inventory stores them.
 */
class FindLoot(
    private val lootDropRepository: LootDropRepository,
    private val eventBus: EventBus,
) {
    /** Path: the tiles walked from the origin (excluded) to the destination (included). */
    operator fun invoke(
        battleUnitId: String,
        path: List<PositionDto>,
    ) {
        val lootDrops = lootDropRepository.searchAll()
        val lootFoundEvents =
            path.mapNotNull { position ->
                val lootableItemIds = lootDrops.flatMap { lootDrop -> lootDrop.droppedItems(position.row, position.column) }
                if (lootableItemIds.isEmpty()) return@mapNotNull null
                LootFound(battleUnitId = battleUnitId, lootableItemIds = lootableItemIds, row = position.row, column = position.column)
            }
        lootFoundEvents.forEach(eventBus::publish)
    }
}
