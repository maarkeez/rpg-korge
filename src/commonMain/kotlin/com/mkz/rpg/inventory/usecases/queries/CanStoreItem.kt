package com.mkz.rpg.inventory.usecases.queries

import com.mkz.rpg.inventory.domain.Inventory
import com.mkz.rpg.inventory.domain.InventoryRepository

/** Whether the inventory of a battle unit has space for one more item. */
class CanStoreItem(
    private val inventoryRepository: InventoryRepository,
) {
    operator fun invoke(battleUnitId: String): Boolean = (inventoryRepository.searchByBattleUnitId(battleUnitId) ?: Inventory.create(battleUnitId)).canStore()
}
