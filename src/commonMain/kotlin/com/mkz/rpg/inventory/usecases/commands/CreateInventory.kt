package com.mkz.rpg.inventory.usecases.commands

import com.mkz.rpg.inventory.domain.Inventory
import com.mkz.rpg.inventory.domain.InventoryRepository
import com.mkz.rpg.unit.usecases.queries.SearchUnitById

/**
 * Creates the empty inventory of a party member, with the starting gold of its unit. Only the party members have one, so CPU battle units and
 * summoned battle units have none. Nothing happens when it already has one.
 */
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
