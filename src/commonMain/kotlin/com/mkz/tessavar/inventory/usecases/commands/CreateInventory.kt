package com.mkz.tessavar.inventory.usecases.commands

import com.mkz.tessavar.inventory.domain.Inventory
import com.mkz.tessavar.inventory.domain.InventoryRepository
import com.mkz.tessavar.unit.usecases.queries.SearchUnitById

class CreateInventory(
    private val inventoryRepository: InventoryRepository,
    private val searchUnitById: SearchUnitById,
) {
    operator fun invoke(
        battleUnitId: String,
        unitId: String,
    ) {
        if (inventoryRepository.searchByBattleUnitId(battleUnitId) != null) return
        val startingGold = searchUnitById(unitId)?.startingGold ?: 0
        inventoryRepository.create(Inventory.create(battleUnitId, gold = startingGold))
    }
}
