package com.mkz.rpg.inventory.usecases.commands

import com.mkz.rpg.battleUnit.usecases.queries.SearchBattleUnitById
import com.mkz.rpg.inventory.domain.Inventory
import com.mkz.rpg.inventory.domain.InventoryError.InventoryBattleUnitDoesNotExist
import com.mkz.rpg.inventory.domain.InventoryError.InventoryLootableItemDoesNotExist
import com.mkz.rpg.inventory.domain.InventoryRepository
import com.mkz.rpg.lootableItem.usecases.queries.SearchLootableItemById
import com.mkz.rpg.player.domain.Player.Dto.PlayerTypeDto.HUMAN
import com.mkz.rpg.player.usecases.queries.SearchPlayerById
import com.mkz.rpg.shared.domain.EventBus

/**
 * Stores an item lying on a tile in the inventory of a battle unit of the human player, creating the inventory with its first item. CPU battle
 * units and summoned battle units have none, so the item stays on its tile.
 */
class StoreLootedItem(
    private val inventoryRepository: InventoryRepository,
    private val searchBattleUnitById: SearchBattleUnitById,
    private val searchPlayerById: SearchPlayerById,
    private val searchLootableItemById: SearchLootableItemById,
    private val eventBus: EventBus,
) {
    operator fun invoke(
        battleUnitId: String,
        lootableItemId: String,
        row: Int,
        column: Int,
    ) {
        val battleUnit = searchBattleUnitById(battleUnitId) ?: throw InventoryBattleUnitDoesNotExist()
        if (searchPlayerById(battleUnit.playerId)?.type != HUMAN) return
        if (battleUnit.remainingTurns != null) return
        searchLootableItemById(lootableItemId) ?: throw InventoryLootableItemDoesNotExist()
        val inventory = inventoryRepository.searchByBattleUnitId(battleUnitId) ?: Inventory.create(battleUnitId)
        val (events, updatedInventory) = inventory.store(lootableItemId, row, column).pullEvents()
        inventoryRepository.create(updatedInventory)
        eventBus.publish(events)
    }
}
