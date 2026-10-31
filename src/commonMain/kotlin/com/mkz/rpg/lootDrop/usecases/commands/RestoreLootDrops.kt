package com.mkz.rpg.lootDrop.usecases.commands

import com.mkz.rpg.lootDrop.domain.LootDrop
import com.mkz.rpg.lootDrop.domain.LootDropRepository

/**
 * Puts back the items a save took, by the unit that dropped them, publishing no events: a unit it names nothing for
 * drops nothing yet. The loot tables are read from the resources again, so it fails when a unit no longer drops loot.
 */
class RestoreLootDrops(
    private val lootDropRepository: LootDropRepository,
) {
    operator fun invoke(droppedItems: Map<String, Map<LootDrop.Dto.PositionDto, List<String>>>) {
        droppedItems.keys.forEach { unitId -> requireNotNull(lootDropRepository.searchByUnitId(unitId)) { "Unit $unitId drops no loot" } }
        lootDropRepository.searchAll().forEach { lootDrop -> lootDropRepository.update(lootDrop.restoreDroppedItems(droppedItems[lootDrop.toDto().unitId].orEmpty())) }
    }
}
