package com.mkz.rpg.lootDrop.usecases.commands

import com.mkz.rpg.battleUnit.usecases.queries.SearchBattleUnitById
import com.mkz.rpg.lootDrop.domain.LootDropError.LootDropLootTableDoesNotExist
import com.mkz.rpg.lootDrop.domain.LootDropEvent.LootDropped
import com.mkz.rpg.lootDrop.domain.LootDropRepository
import com.mkz.rpg.lootTable.usecases.queries.SearchLootTableById
import com.mkz.rpg.shared.domain.EventBus
import kotlin.random.Random

/** Drops the loot of a defeated battle unit on the tile where it was defeated, unless its unit drops nothing. */
class DropLoot(
    private val searchBattleUnitById: SearchBattleUnitById,
    private val lootDropRepository: LootDropRepository,
    private val searchLootTableById: SearchLootTableById,
    private val random: Random,
    private val eventBus: EventBus,
) {
    operator fun invoke(
        battleUnitId: String,
        defeatedAtRow: Int,
        defeatedAtColumn: Int,
    ) {
        val unitId = searchBattleUnitById(battleUnitId)?.unitId ?: return
        val lootDrop = lootDropRepository.searchByUnitId(unitId) ?: return
        val lootTables = lootDrop.toDto().lootTableIds.map { lootTableId -> searchLootTableById(lootTableId) ?: throw LootDropLootTableDoesNotExist() }
        val lootableItemId = lootDrop.drop(lootTables, random) ?: return
        eventBus.publish(LootDropped(lootableItemId = lootableItemId, row = defeatedAtRow, column = defeatedAtColumn))
    }
}
