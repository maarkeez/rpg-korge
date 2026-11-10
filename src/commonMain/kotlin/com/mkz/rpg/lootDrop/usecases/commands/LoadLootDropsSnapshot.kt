package com.mkz.rpg.lootDrop.usecases.commands

import com.mkz.rpg.lootDrop.domain.LootDrop
import com.mkz.rpg.lootDrop.domain.LootDropRepository

class LoadLootDropsSnapshot(
    private val lootDropRepository: LootDropRepository,
) {
    operator fun invoke(droppedItems: Map<String, Map<LootDrop.Dto.PositionDto, List<String>>>) {
        droppedItems.keys.forEach { unitId -> requireNotNull(lootDropRepository.searchByUnitId(unitId)) { "Unit $unitId drops no loot" } }
        lootDropRepository.searchAll().forEach { lootDrop -> lootDropRepository.update(lootDrop.loadDroppedItems(droppedItems[lootDrop.toDto().unitId].orEmpty())) }
    }
}
