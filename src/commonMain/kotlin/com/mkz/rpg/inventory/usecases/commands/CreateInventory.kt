package com.mkz.rpg.inventory.usecases.commands

import com.mkz.rpg.battleUnit.usecases.queries.SearchBattleUnitById
import com.mkz.rpg.inventory.domain.Inventory
import com.mkz.rpg.inventory.domain.InventoryError.InventoryBattleUnitDoesNotExist
import com.mkz.rpg.inventory.domain.InventoryRepository
import com.mkz.rpg.player.domain.Player.Dto.PlayerTypeDto.HUMAN
import com.mkz.rpg.player.usecases.queries.SearchPlayerById
import com.mkz.rpg.unit.usecases.queries.SearchUnitById

/**
 * Creates the empty inventory of a deployed battle unit of the human player, with the starting gold of its unit. CPU battle units and summoned
 * battle units have none. Nothing happens when it already has one.
 */
class CreateInventory(
    private val inventoryRepository: InventoryRepository,
    private val searchBattleUnitById: SearchBattleUnitById,
    private val searchPlayerById: SearchPlayerById,
    private val searchUnitById: SearchUnitById,
) {
    operator fun invoke(battleUnitId: String) {
        val battleUnit = searchBattleUnitById(battleUnitId) ?: throw InventoryBattleUnitDoesNotExist()
        if (searchPlayerById(battleUnit.playerId)?.type != HUMAN) return
        if (battleUnit.remainingTurns != null) return
        if (inventoryRepository.searchByBattleUnitId(battleUnitId) != null) return
        val startingGold = searchUnitById(battleUnit.unitId)?.startingGold ?: 0
        inventoryRepository.create(Inventory.create(battleUnitId, gold = startingGold))
    }
}
