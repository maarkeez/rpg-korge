package com.mkz.rpg.lootDrop.adapters.presentation

import com.mkz.rpg.lootDrop.adapters.events.OnBattleUnitDefeated
import com.mkz.rpg.lootDrop.adapters.events.OnBattleUnitMoved
import com.mkz.rpg.lootDrop.adapters.events.OnRequestInitialiseLootDrops
import com.mkz.rpg.lootDrop.adapters.events.OnRequestPickUpDroppedItem
import com.mkz.rpg.lootDrop.adapters.resources.ResourcesLootDropLoader
import com.mkz.rpg.lootDrop.adapters.save.LootDropSnapshottable
import com.mkz.rpg.lootDrop.adapters.storage.InMemoryLootDropRepository
import com.mkz.rpg.lootDrop.domain.LootDropRepository
import com.mkz.rpg.lootDrop.usecases.commands.DropLoot
import com.mkz.rpg.lootDrop.usecases.commands.FindLoot
import com.mkz.rpg.lootDrop.usecases.commands.InitialiseLootDrops
import com.mkz.rpg.lootDrop.usecases.commands.PickUpDroppedItem
import com.mkz.rpg.lootDrop.usecases.commands.PlaceDroppedItem
import com.mkz.rpg.lootDrop.usecases.queries.SearchDroppedItems
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
    private val lootDropLoader: LootDropLoader = ResourcesLootDropLoader(),
    random: Random = Random.Default,
) {
    // Storage
    private val lootDropRepository: LootDropRepository = InMemoryLootDropRepository()

    // Save
    val snapshottable: Snapshottable = LootDropSnapshottable(lootDropRepository)

    // Commands
    val initialiseLootDrops = InitialiseLootDrops(lootDropLoader, lootDropRepository, unitApi.searchUnitById, lootTableApi.searchLootTableById, eventBus)
    val dropLoot = DropLoot(lootDropRepository, lootTableApi.searchLootTableById, random, eventBus)
    val placeDroppedItem = PlaceDroppedItem(lootDropRepository, eventBus)
    val findLoot = FindLoot(lootDropRepository, eventBus)
    val pickUpDroppedItem = PickUpDroppedItem(lootDropRepository, eventBus)

    // Queries
    val searchDroppedItems = SearchDroppedItems(lootDropRepository)

    // Events
    private val onRequestInitialiseLootDrops = OnRequestInitialiseLootDrops(initialiseLootDrops, eventBus)
    private val onBattleUnitDefeated = OnBattleUnitDefeated(dropLoot, eventBus)
    private val onBattleUnitMoved = OnBattleUnitMoved(findLoot, eventBus)
    private val onRequestPickUpDroppedItem = OnRequestPickUpDroppedItem(pickUpDroppedItem, eventBus)

    suspend fun init() {
        lootDropLoader.initResources()
    }
}
