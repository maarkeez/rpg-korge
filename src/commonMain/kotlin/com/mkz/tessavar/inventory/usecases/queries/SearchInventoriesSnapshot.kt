package com.mkz.tessavar.inventory.usecases.queries

import com.mkz.tessavar.inventory.domain.Inventory
import com.mkz.tessavar.inventory.domain.InventoryRepository

class SearchInventoriesSnapshot(
    private val inventoryRepository: InventoryRepository,
) {
    operator fun invoke(): List<Inventory.Dto> = inventoryRepository.searchAll().map(Inventory::toDto)
}
