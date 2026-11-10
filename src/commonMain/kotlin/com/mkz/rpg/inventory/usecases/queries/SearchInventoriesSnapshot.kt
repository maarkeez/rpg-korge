package com.mkz.rpg.inventory.usecases.queries

import com.mkz.rpg.inventory.domain.Inventory
import com.mkz.rpg.inventory.domain.InventoryRepository

class SearchInventoriesSnapshot(
    private val inventoryRepository: InventoryRepository,
) {
    operator fun invoke(): List<Inventory.Dto> = inventoryRepository.searchAll().map(Inventory::toDto)
}
