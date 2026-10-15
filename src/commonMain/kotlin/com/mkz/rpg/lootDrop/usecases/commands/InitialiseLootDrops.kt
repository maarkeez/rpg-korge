package com.mkz.rpg.lootDrop.usecases.commands

import com.mkz.rpg.lootDrop.domain.LootDrop
import com.mkz.rpg.lootDrop.domain.LootDropError.LootDropLootTableDoesNotExist
import com.mkz.rpg.lootDrop.domain.LootDropError.LootDropUnitDoesNotExist
import com.mkz.rpg.lootDrop.domain.LootDropEvent
import com.mkz.rpg.lootDrop.domain.LootDropRepository
import com.mkz.rpg.lootDrop.usecases.services.LootDropLoader
import com.mkz.rpg.lootTable.usecases.queries.SearchLootTableById
import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.unit.usecases.queries.SearchUnitById

/** Creates every loaded loot drop, unless it already exists, once its unit and all its loot tables exist. */
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
        eventBus.publish(events)
    }
}
