package com.mkz.rpg.inventory.usecases.queries

import com.mkz.rpg.inventory.domain.Inventory
import com.mkz.rpg.inventory.domain.InventoryRepository

class SearchInventoryByBattleUnitId(
    private val inventoryRepository: InventoryRepository,
) {
    operator fun invoke(battleUnitId: String): Inventory.Dto? = inventoryRepository.searchByBattleUnitId(battleUnitId)?.toDto()
}
