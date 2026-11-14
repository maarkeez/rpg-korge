package com.mkz.tessavar.inventory.usecases.queries

import com.mkz.tessavar.inventory.domain.Inventory
import com.mkz.tessavar.inventory.domain.InventoryRepository

class SearchInventoryByBattleUnitId(
    private val inventoryRepository: InventoryRepository,
) {
    operator fun invoke(battleUnitId: String): Inventory.Dto? = inventoryRepository.searchByBattleUnitId(battleUnitId)?.toDto()
}
