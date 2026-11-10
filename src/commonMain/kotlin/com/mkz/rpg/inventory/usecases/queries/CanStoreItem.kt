package com.mkz.rpg.inventory.usecases.queries

import com.mkz.rpg.inventory.domain.InventoryRepository

class CanStoreItem(
    private val inventoryRepository: InventoryRepository,
) {
    operator fun invoke(battleUnitId: String): Boolean = inventoryRepository.searchByBattleUnitId(battleUnitId)?.canStore() ?: false
}
