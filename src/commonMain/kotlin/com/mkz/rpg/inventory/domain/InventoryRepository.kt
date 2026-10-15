package com.mkz.rpg.inventory.domain

interface InventoryRepository {
    fun create(inventory: Inventory)

    fun searchByBattleUnitId(battleUnitId: String): Inventory?
}
