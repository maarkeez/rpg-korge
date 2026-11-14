package com.mkz.tessavar.lootDrop.usecases.commands

import com.mkz.tessavar.lootDrop.domain.LootDrop
import com.mkz.tessavar.lootDrop.domain.LootDropError.LootDropLootTableDoesNotExist
import com.mkz.tessavar.lootDrop.domain.LootDropError.LootDropUnitDoesNotExist
import com.mkz.tessavar.lootDrop.domain.LootDropEvent
import com.mkz.tessavar.lootDrop.domain.LootDropRepository
import com.mkz.tessavar.lootDrop.usecases.services.LootDropLoader
import com.mkz.tessavar.lootTable.usecases.queries.SearchLootTableById
import com.mkz.tessavar.shared.domain.EventBus
import com.mkz.tessavar.unit.usecases.queries.SearchUnitById

class InitialiseLootDrops(
    private val lootDropLoader: LootDropLoader,
    private val lootDropRepository: LootDropRepository,
    private val searchUnitById: SearchUnitById,
    private val searchLootTableById: SearchLootTableById,
    private val eventBus: EventBus,
) {
    operator fun invoke() {
        val events = mutableSetOf<LootDropEvent>()
        lootDropLoader.loadLootDrops().forEach { lootDropDto ->
            if (lootDropRepository.searchByUnitId(lootDropDto.unitId) != null) return@forEach
            if (searchUnitById(lootDropDto.unitId) == null) throw LootDropUnitDoesNotExist()
            if (lootDropDto.lootTableIds.any { lootTableId -> searchLootTableById(lootTableId) == null }) throw LootDropLootTableDoesNotExist()
            val (lootDropEvents, lootDrop) = LootDrop.create(lootDropDto).pullEvents()
            lootDropRepository.create(lootDrop)
            events += lootDropEvents
        }
        if (lootDropRepository.searchByUnitId(LootDrop.OWNERLESS_UNIT_ID) == null) lootDropRepository.create(LootDrop.ownerless())
        eventBus.publish(events)
    }
}
