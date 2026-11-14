package com.mkz.tessavar.shared.adapters.presentation

import com.mkz.tessavar.ability.adapters.presentation.AbilityApi
import com.mkz.tessavar.ability.domain.Ability
import com.mkz.tessavar.ability.usecases.services.FakeAbilityLoader
import com.mkz.tessavar.battle.adapters.presentation.BattleApi
import com.mkz.tessavar.battle.adapters.presentation.startBattleWithEveryBattleUnit
import com.mkz.tessavar.battleUnit.adapters.presentation.BattleUnitApi
import com.mkz.tessavar.battleUnit.usecases.services.BattleUnitLoader.Deployment
import com.mkz.tessavar.battleUnit.usecases.services.FakeBattleUnitLoader
import com.mkz.tessavar.battlefield.adapters.presentation.BattlefieldApi
import com.mkz.tessavar.battlefield.adapters.resources.TomlBattlefieldDecoder
import com.mkz.tessavar.battlefield.adapters.resources.TomlBattlefieldEncoder
import com.mkz.tessavar.battlefield.usecases.services.FakeBattlefieldLoader
import com.mkz.tessavar.building.adapters.presentation.BuildingApi
import com.mkz.tessavar.building.domain.Building
import com.mkz.tessavar.building.usecases.services.FakeBuildingLoader
import com.mkz.tessavar.effect.adapters.presentation.EffectApi
import com.mkz.tessavar.effect.domain.Effect
import com.mkz.tessavar.effect.usecases.services.FakeEffectLoader
import com.mkz.tessavar.inventory.adapters.presentation.InventoryApi
import com.mkz.tessavar.lootDrop.adapters.presentation.LootDropApi
import com.mkz.tessavar.lootDrop.domain.LootDrop
import com.mkz.tessavar.lootDrop.usecases.services.FakeLootDropLoader
import com.mkz.tessavar.lootTable.adapters.presentation.LootTableApi
import com.mkz.tessavar.lootTable.domain.LootTable
import com.mkz.tessavar.lootTable.usecases.services.FakeLootTableLoader
import com.mkz.tessavar.lootableItem.adapters.presentation.LootableItemApi
import com.mkz.tessavar.lootableItem.domain.LootableItem
import com.mkz.tessavar.lootableItem.usecases.services.FakeLootableItemLoader
import com.mkz.tessavar.npc.adapters.presentation.NpcApi
import com.mkz.tessavar.npc.domain.Npc
import com.mkz.tessavar.npc.usecases.services.FakeNpcLoader
import com.mkz.tessavar.party.adapters.presentation.PartyApi
import com.mkz.tessavar.player.adapters.presentation.PlayerApi
import com.mkz.tessavar.player.domain.Player
import com.mkz.tessavar.player.usecases.services.FakePlayerLoader
import com.mkz.tessavar.quest.adapters.presentation.QuestApi
import com.mkz.tessavar.quest.domain.Quest
import com.mkz.tessavar.quest.usecases.services.FakeQuestLoader
import com.mkz.tessavar.shared.adapters.events.InMemoryEventBus
import com.mkz.tessavar.terrain.adapters.presentation.TerrainApi
import com.mkz.tessavar.terrain.domain.Terrain
import com.mkz.tessavar.terrain.usecases.services.FakeTerrainLoader
import com.mkz.tessavar.unit.adapters.presentation.UnitApi
import com.mkz.tessavar.unit.domain.Unit
import com.mkz.tessavar.unit.usecases.services.FakeUnitLoader
import kotlin.random.Random

class SubdomainApis(
    val eventBus: InMemoryEventBus = InMemoryEventBus(),
    terrains: List<Terrain.Dto> = emptyList(),
    effects: List<Effect.Dto> = emptyList(),
    lootableItems: List<LootableItem.Dto> = emptyList(),
    lootTables: List<LootTable.Dto> = emptyList(),
    abilities: List<Ability.Dto> = emptyList(),
    units: List<Unit.Dto> = emptyList(),
    lootDrops: List<LootDrop.Dto> = emptyList(),
    players: List<Player.Dto> = emptyList(),
    private val tiles: List<List<String>> = emptyList(),
    buildings: List<Building.Dto> = emptyList(),
    deployments: List<Deployment> = emptyList(),
    npcs: List<Npc.Dto> = emptyList(),
    private val quests: List<Quest.Dto> = emptyList(),
    private val playerOrder: List<String> = emptyList(),
    private val explore: Boolean = false,
) {
    val terrainApi = TerrainApi(eventBus, FakeTerrainLoader(terrains))
    val unitApi = UnitApi(eventBus, FakeUnitLoader(units))
    val playerApi = PlayerApi(eventBus, FakePlayerLoader(players))
    val battlefieldApi = BattlefieldApi(terrainApi, eventBus, FakeBattlefieldLoader(tiles), TomlBattlefieldEncoder(), TomlBattlefieldDecoder())
    val buildingApi = BuildingApi(battlefieldApi, eventBus, FakeBuildingLoader(buildings))
    val effectApi = EffectApi(eventBus, FakeEffectLoader(effects))
    val abilityApi = AbilityApi(effectApi, eventBus, FakeAbilityLoader(abilities))
    val lootableItemApi = LootableItemApi(effectApi, eventBus, FakeLootableItemLoader(lootableItems))
    val lootTableApi = LootTableApi(lootableItemApi, eventBus, FakeLootTableLoader(lootTables))
    val battleUnitApi = BattleUnitApi(effectApi, abilityApi, unitApi, playerApi, battlefieldApi, eventBus, FakeBattleUnitLoader(deployments))
    val lootDropApi = LootDropApi(unitApi, lootTableApi, eventBus, FakeLootDropLoader(lootDrops), Random.Default)
    val battleApi = BattleApi(eventBus)
    val partyApi = PartyApi(playerApi, battleUnitApi, battlefieldApi, eventBus)
    val npcApi = NpcApi(battlefieldApi, lootableItemApi, partyApi, eventBus, FakeNpcLoader(npcs))
    val inventoryApi = InventoryApi(effectApi, lootableItemApi, unitApi, battleApi, partyApi, npcApi, eventBus)
    val questApi = QuestApi(battlefieldApi, partyApi, eventBus, FakeQuestLoader(quests))

    fun initialise() {
        terrainApi.initialiseTerrains()
        effectApi.initialiseEffects()
        lootableItemApi.initialiseLootableItems()
        lootTableApi.initialiseLootTables()
        abilityApi.initialiseAbilities()
        unitApi.initialiseUnits()
        lootDropApi.initialiseLootDrops()
        playerApi.initialisePlayers()
        if (tiles.isNotEmpty()) battlefieldApi.initialiseBattlefield()
        eventBus.dispatch()
        buildingApi.placeBuildings()
        eventBus.dispatch()
        if (quests.isNotEmpty()) questApi.createQuests()
        battleUnitApi.initialiseBattleUnits()
        eventBus.dispatch()
        npcApi.placeNpcs()
        eventBus.dispatch()
        if (explore) partyApi.startExploration()
        eventBus.dispatch()
        if (playerOrder.isNotEmpty()) battleApi.startBattleWithEveryBattleUnit(battleUnitApi, playerOrder[0], playerOrder[1])
        eventBus.dispatch()
    }
}
