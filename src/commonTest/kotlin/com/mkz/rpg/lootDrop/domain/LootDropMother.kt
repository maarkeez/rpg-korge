package com.mkz.rpg.lootDrop.domain

import korlibs.io.util.UUID

object LootDropMother {
    fun lootDrop(
        unitId: String = unitId(),
        lootTableIds: List<String> = listOf(lootTableId()),
    ): LootDrop =
        LootDrop
            .create(dto = LootDrop.Dto(unitId = unitId, lootTableIds = lootTableIds))
            .pullEvents()
            .second

    fun unitId(): String = "loot-drop-unit-" + UUID.randomUUID().toString()

    fun lootTableId(): String = "loot-drop-loot-table-" + UUID.randomUUID().toString()
}
