package com.mkz.tessavar.inventory.usecases.queries

import com.mkz.tessavar.inventory.domain.InventoryRepository

class CanStoreItem(
    private val inventoryRepository: InventoryRepository,
) {
    operator fun invoke(battleUnitId: String): Boolean = inventoryRepository.searchByBattleUnitId(battleUnitId)?.canStore() ?: false
}
