package com.mkz.rpg.inventory.usecases.queries

import com.mkz.rpg.inventory.domain.Inventory
import com.mkz.rpg.inventory.domain.InventoryRepository

/** Returns `null` for the battle units without inventory: the CPU battle units and the summoned ones. */
class SearchInventoryByBattleUnitId(
    private val inventoryRepository: InventoryRepository,
) {
    operator fun invoke(battleUnitId: String): Inventory.Dto? = inventoryRepository.searchByBattleUnitId(battleUnitId)?.toDto()
}
