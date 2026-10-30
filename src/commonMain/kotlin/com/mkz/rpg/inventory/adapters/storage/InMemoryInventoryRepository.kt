package com.mkz.rpg.inventory.adapters.storage

import com.mkz.rpg.inventory.domain.Inventory
import com.mkz.rpg.inventory.domain.InventoryRepository

class InMemoryInventoryRepository : InventoryRepository {
    private val inventories = mutableMapOf<String, Inventory>()

    override fun create(inventory: Inventory) {
        inventories[inventory.toDto().battleUnitId] = inventory
    }

    override fun searchByBattleUnitId(battleUnitId: String) = inventories[battleUnitId]

    override fun searchAll(): List<Inventory> = inventories.values.toList()

    override fun deleteAll() = inventories.clear()
}
