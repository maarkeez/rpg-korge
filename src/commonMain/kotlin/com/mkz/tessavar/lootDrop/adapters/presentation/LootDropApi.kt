package com.mkz.tessavar.lootDrop.adapters.presentation

import com.mkz.tessavar.lootDrop.adapters.events.OnBattleUnitDefeated
import com.mkz.tessavar.lootDrop.adapters.events.OnBattleUnitMoved
import com.mkz.tessavar.lootDrop.adapters.events.OnRequestDropItems
import com.mkz.tessavar.lootDrop.adapters.events.OnRequestInitialiseLootDrops
import com.mkz.tessavar.lootDrop.adapters.events.OnRequestLootDroppedItem
import com.mkz.tessavar.lootDrop.adapters.save.LootDropSnapshottable
import com.mkz.tessavar.lootDrop.adapters.storage.InMemoryLootDropRepository
import com.mkz.tessavar.lootDrop.domain.LootDropRepository
import com.mkz.tessavar.lootDrop.usecases.commands.DropItemWithoutRolling
import com.mkz.tessavar.lootDrop.usecases.commands.DropItems
import com.mkz.tessavar.lootDrop.usecases.commands.FindDroppedItems
import com.mkz.tessavar.lootDrop.usecases.commands.InitialiseLootDrops
import com.mkz.tessavar.lootDrop.usecases.commands.LoadLootDropsSnapshot
import com.mkz.tessavar.lootDrop.usecases.commands.LootDroppedItem
import com.mkz.tessavar.lootDrop.usecases.commands.RollLootDrop
import com.mkz.tessavar.lootDrop.usecases.queries.SearchDroppedItems
import com.mkz.tessavar.lootDrop.usecases.queries.SearchLootDropsSnapshot
import com.mkz.tessavar.lootDrop.usecases.services.LootDropLoader
import com.mkz.tessavar.lootTable.adapters.presentation.LootTableApi
import com.mkz.tessavar.shared.domain.EventBus
import com.mkz.tessavar.shared.domain.Snapshottable
import com.mkz.tessavar.unit.adapters.presentation.UnitApi
import kotlin.random.Random

class LootDropApi(
    unitApi: UnitApi,
    lootTableApi: LootTableApi,
    eventBus: EventBus,
    private val lootDropLoader: LootDropLoader,
    random: Random,
) {
    private val lootDropRepository: LootDropRepository = InMemoryLootDropRepository()

    private val searchLootDropsSnapshot = SearchLootDropsSnapshot(lootDropRepository)
    private val loadLootDropsSnapshot = LoadLootDropsSnapshot(lootDropRepository)
    val snapshottable: Snapshottable = LootDropSnapshottable(searchLootDropsSnapshot, loadLootDropsSnapshot)

    val initialiseLootDrops = InitialiseLootDrops(lootDropLoader, lootDropRepository, unitApi.searchUnitById, lootTableApi.searchLootTableById, eventBus)
    val rollLootDrop = RollLootDrop(lootDropRepository, lootTableApi.searchLootTableById, random, eventBus)
    val dropItems = DropItems(lootDropRepository, eventBus)
    val dropItemWithoutRolling = DropItemWithoutRolling(lootDropRepository, eventBus)
    val findDroppedItems = FindDroppedItems(lootDropRepository, eventBus)
    val lootDroppedItem = LootDroppedItem(lootDropRepository, eventBus)

    val searchDroppedItems = SearchDroppedItems(lootDropRepository)

    private val onRequestInitialiseLootDrops = OnRequestInitialiseLootDrops(initialiseLootDrops, eventBus)
    private val onBattleUnitDefeated = OnBattleUnitDefeated(rollLootDrop, eventBus)
    private val onBattleUnitMoved = OnBattleUnitMoved(findDroppedItems, eventBus)
    private val onRequestLootDroppedItem = OnRequestLootDroppedItem(lootDroppedItem, eventBus)
    private val onRequestDropItems = OnRequestDropItems(dropItems, eventBus)

    suspend fun init() {
        lootDropLoader.initResources()
    }
}
