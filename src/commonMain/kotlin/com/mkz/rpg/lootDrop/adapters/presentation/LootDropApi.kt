package com.mkz.rpg.lootDrop.adapters.presentation

import com.mkz.rpg.lootDrop.adapters.events.OnBattleUnitDefeated
import com.mkz.rpg.lootDrop.adapters.events.OnBattleUnitMoved
import com.mkz.rpg.lootDrop.adapters.events.OnRequestDropItems
import com.mkz.rpg.lootDrop.adapters.events.OnRequestInitialiseLootDrops
import com.mkz.rpg.lootDrop.adapters.events.OnRequestLootDroppedItem
import com.mkz.rpg.lootDrop.adapters.save.LootDropSnapshottable
import com.mkz.rpg.lootDrop.adapters.storage.InMemoryLootDropRepository
import com.mkz.rpg.lootDrop.domain.LootDropRepository
import com.mkz.rpg.lootDrop.usecases.commands.DropItemWithoutRolling
import com.mkz.rpg.lootDrop.usecases.commands.DropItems
import com.mkz.rpg.lootDrop.usecases.commands.FindDroppedItems
import com.mkz.rpg.lootDrop.usecases.commands.InitialiseLootDrops
import com.mkz.rpg.lootDrop.usecases.commands.LoadLootDropsSnapshot
import com.mkz.rpg.lootDrop.usecases.commands.LootDroppedItem
import com.mkz.rpg.lootDrop.usecases.commands.RollLootDrop
import com.mkz.rpg.lootDrop.usecases.queries.SearchDroppedItems
import com.mkz.rpg.lootDrop.usecases.queries.SearchLootDropsSnapshot
import com.mkz.rpg.lootDrop.usecases.services.LootDropLoader
import com.mkz.rpg.lootTable.adapters.presentation.LootTableApi
import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.shared.domain.Snapshottable
import com.mkz.rpg.unit.adapters.presentation.UnitApi
import kotlin.random.Random

class LootDropApi(
    unitApi: UnitApi,
    lootTableApi: LootTableApi,
    eventBus: EventBus,
    private val lootDropLoader: LootDropLoader,
    random: Random,
) {
    // Storage
    private val lootDropRepository: LootDropRepository = InMemoryLootDropRepository()

    // Save
    private val searchLootDropsSnapshot = SearchLootDropsSnapshot(lootDropRepository)
    private val loadLootDropsSnapshot = LoadLootDropsSnapshot(lootDropRepository)
    val snapshottable: Snapshottable = LootDropSnapshottable(searchLootDropsSnapshot, loadLootDropsSnapshot)

    // Commands
    val initialiseLootDrops = InitialiseLootDrops(lootDropLoader, lootDropRepository, unitApi.searchUnitById, lootTableApi.searchLootTableById, eventBus)
    val rollLootDrop = RollLootDrop(lootDropRepository, lootTableApi.searchLootTableById, random, eventBus)
    val dropItems = DropItems(lootDropRepository, eventBus)
    val dropItemWithoutRolling = DropItemWithoutRolling(lootDropRepository, eventBus)
    val findDroppedItems = FindDroppedItems(lootDropRepository, eventBus)
    val lootDroppedItem = LootDroppedItem(lootDropRepository, eventBus)

    // Queries
    val searchDroppedItems = SearchDroppedItems(lootDropRepository)

    // Events
    private val onRequestInitialiseLootDrops = OnRequestInitialiseLootDrops(initialiseLootDrops, eventBus)
    private val onBattleUnitDefeated = OnBattleUnitDefeated(rollLootDrop, eventBus)
    private val onBattleUnitMoved = OnBattleUnitMoved(findDroppedItems, eventBus)
    private val onRequestLootDroppedItem = OnRequestLootDroppedItem(lootDroppedItem, eventBus)
    private val onRequestDropItems = OnRequestDropItems(dropItems, eventBus)

    suspend fun init() {
        lootDropLoader.initResources()
    }
}
