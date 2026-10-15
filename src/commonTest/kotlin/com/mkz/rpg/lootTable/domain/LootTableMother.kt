package com.mkz.rpg.lootTable.domain

import com.mkz.rpg.lootTable.domain.LootTable.Dto.EntryDto
import korlibs.io.util.UUID

object LootTableMother {
    fun lootTable(
        id: String = lootTableId(),
        entries: List<EntryDto> = listOf(lootableItemEntry(), nothingEntry()),
    ): LootTable =
        LootTable
            .create(dto = LootTable.Dto(id = id, entries = entries))
            .pullEvents()
            .second

    fun lootableItemEntry(
        lootableItemId: String = lootableItemId(),
        weight: Int = 10,
    ): EntryDto = EntryDto.lootableItem(lootableItemId = lootableItemId, weight = weight)

    fun nothingEntry(weight: Int = 5): EntryDto = EntryDto.nothing(weight = weight)

    fun lootTableId(): String = "loot-table-" + UUID.randomUUID().toString()

    fun lootableItemId(): String = "loot-table-lootable-item-" + UUID.randomUUID().toString()
}
