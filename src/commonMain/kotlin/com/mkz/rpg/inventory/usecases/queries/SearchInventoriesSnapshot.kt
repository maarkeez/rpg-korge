package com.mkz.rpg.inventory.usecases.queries

import com.mkz.rpg.inventory.domain.Inventory
import com.mkz.rpg.inventory.domain.InventoryRepository

/** What is saved of the inventory of every battle unit: its stored and equipped items, and its gold. */
class SearchInventoriesSnapshot(
    private val inventoryRepository: InventoryRepository,
) {
    operator fun invoke(): List<Inventory.Dto> = inventoryRepository.searchAll().map(Inventory::toDto)
}
