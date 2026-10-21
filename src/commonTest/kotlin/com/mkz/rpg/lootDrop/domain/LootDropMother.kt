package com.mkz.rpg.lootDrop.domain

import korlibs.io.util.UUID

object LootDropMother {
    /** The [droppedItems] lie on their tiles in the given order. */
    fun lootDrop(
        unitId: String = unitId(),
        lootTableIds: List<String> = listOf(lootTableId()),
        droppedItems: Map<LootDrop.Dto.PositionDto, List<String>> = emptyMap(),
    ): LootDrop =
        LootDrop
            .create(dto = LootDrop.Dto(unitId = unitId, lootTableIds = lootTableIds, droppedItems = droppedItems))
            .pullEvents()
            .second

    fun unitId(): String = "loot-drop-unit-" + UUID.randomUUID().toString()

    fun lootTableId(): String = "loot-drop-loot-table-" + UUID.randomUUID().toString()
}
