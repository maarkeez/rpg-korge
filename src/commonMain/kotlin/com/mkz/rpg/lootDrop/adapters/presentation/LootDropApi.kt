package com.mkz.rpg.lootDrop.adapters.presentation

import com.mkz.rpg.battleUnit.adapters.presentation.BattleUnitApi
import com.mkz.rpg.lootDrop.adapters.events.OnBattleUnitDefeated
import com.mkz.rpg.lootDrop.adapters.events.OnRequestInitialiseLootDrops
import com.mkz.rpg.lootDrop.adapters.resources.ResourcesLootDropLoader
import com.mkz.rpg.lootDrop.adapters.storage.InMemoryLootDropRepository
import com.mkz.rpg.lootDrop.domain.LootDropRepository
import com.mkz.rpg.lootDrop.usecases.commands.DropLoot
import com.mkz.rpg.lootDrop.usecases.commands.InitialiseLootDrops
import com.mkz.rpg.lootDrop.usecases.services.LootDropLoader
import com.mkz.rpg.lootTable.adapters.presentation.LootTableApi
import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.unit.adapters.presentation.UnitApi
import kotlin.random.Random

class LootDropApi(
    unitApi: UnitApi,
    lootTableApi: LootTableApi,
    battleUnitApi: BattleUnitApi,
    eventBus: EventBus,
    private val lootDropLoader: LootDropLoader = ResourcesLootDropLoader(),
    random: Random = Random.Default,
) {
    // Storage
    private val lootDropRepository: LootDropRepository = InMemoryLootDropRepository()

    // Commands
    val initialiseLootDrops = InitialiseLootDrops(lootDropLoader, lootDropRepository, unitApi.searchUnitById, lootTableApi.searchLootTableById, eventBus)
    val dropLoot = DropLoot(battleUnitApi.searchBattleUnitById, lootDropRepository, lootTableApi.searchLootTableById, random, eventBus)

    // Events
    private val onRequestInitialiseLootDrops = OnRequestInitialiseLootDrops(initialiseLootDrops, eventBus)
    private val onBattleUnitDefeated = OnBattleUnitDefeated(dropLoot, eventBus)

    suspend fun init() {
        lootDropLoader.initResources()
    }
}
