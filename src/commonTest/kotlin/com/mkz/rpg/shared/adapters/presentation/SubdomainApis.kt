package com.mkz.rpg.shared.adapters.presentation

import com.mkz.rpg.ability.adapters.presentation.AbilityApi
import com.mkz.rpg.ability.domain.Ability
import com.mkz.rpg.ability.usecases.services.FakeAbilityLoader
import com.mkz.rpg.battle.adapters.presentation.BattleApi
import com.mkz.rpg.battle.usecases.services.FakeBattleLoader
import com.mkz.rpg.battleUnit.adapters.presentation.BattleUnitApi
import com.mkz.rpg.battleUnit.usecases.services.BattleUnitLoader.Deployment
import com.mkz.rpg.battleUnit.usecases.services.FakeBattleUnitLoader
import com.mkz.rpg.battlefield.adapters.presentation.BattlefieldApi
import com.mkz.rpg.battlefield.usecases.services.FakeBattlefieldLoader
import com.mkz.rpg.effect.adapters.presentation.EffectApi
import com.mkz.rpg.effect.domain.Effect
import com.mkz.rpg.effect.usecases.services.FakeEffectLoader
import com.mkz.rpg.lootTable.adapters.presentation.LootTableApi
import com.mkz.rpg.lootTable.domain.LootTable
import com.mkz.rpg.lootTable.usecases.services.FakeLootTableLoader
import com.mkz.rpg.lootableItem.adapters.presentation.LootableItemApi
import com.mkz.rpg.lootableItem.domain.LootableItem
import com.mkz.rpg.lootableItem.usecases.services.FakeLootableItemLoader
import com.mkz.rpg.player.adapters.presentation.PlayerApi
import com.mkz.rpg.player.domain.Player
import com.mkz.rpg.player.usecases.services.FakePlayerLoader
import com.mkz.rpg.shared.adapters.events.InMemoryEventBus
import com.mkz.rpg.terrain.adapters.presentation.TerrainApi
import com.mkz.rpg.terrain.domain.Terrain
import com.mkz.rpg.terrain.usecases.services.FakeTerrainLoader
import com.mkz.rpg.unit.adapters.presentation.UnitApi
import com.mkz.rpg.unit.domain.Unit
import com.mkz.rpg.unit.usecases.services.FakeUnitLoader

/**
 * The real subdomain APIs sharing one event bus, wired as the game does, which load the given data instead of the resources.
 * Presentation tests use them instead of stubbing the APIs. Call [initialise] to store the data.
 */
class SubdomainApis(
    val eventBus: InMemoryEventBus = InMemoryEventBus(),
    terrains: List<Terrain.Dto> = emptyList(),
    effects: List<Effect.Dto> = emptyList(),
    lootableItems: List<LootableItem.Dto> = emptyList(),
    lootTables: List<LootTable.Dto> = emptyList(),
    abilities: List<Ability.Dto> = emptyList(),
    units: List<Unit.Dto> = emptyList(),
    players: List<Player.Dto> = emptyList(),
    private val tiles: List<List<String>> = emptyList(),
    deployments: List<Deployment> = emptyList(),
    private val playerOrder: List<String> = emptyList(),
) {
    val terrainApi = TerrainApi(eventBus, FakeTerrainLoader(terrains))
    val unitApi = UnitApi(eventBus, FakeUnitLoader(units))
    val playerApi = PlayerApi(eventBus, FakePlayerLoader(players))
    val battlefieldApi = BattlefieldApi(terrainApi, eventBus, FakeBattlefieldLoader(tiles))
    val effectApi = EffectApi(eventBus, FakeEffectLoader(effects))
    val abilityApi = AbilityApi(effectApi, eventBus, FakeAbilityLoader(abilities))
    val lootableItemApi = LootableItemApi(effectApi, eventBus, FakeLootableItemLoader(lootableItems))
    val lootTableApi = LootTableApi(lootableItemApi, eventBus, FakeLootTableLoader(lootTables))
    val battleUnitApi = BattleUnitApi(effectApi, abilityApi, unitApi, playerApi, battlefieldApi, eventBus, FakeBattleUnitLoader(deployments))
    val battleApi = BattleApi(eventBus, battleUnitApi, FakeBattleLoader(playerOrder))

    /** Stores the given data, deploys the battle units and starts the battle, dispatching the events it publishes. */
    fun initialise() {
        terrainApi.initialiseTerrains()
        effectApi.initialiseEffects()
        lootableItemApi.initialiseLootableItems()
        lootTableApi.initialiseLootTables()
        abilityApi.initialiseAbilities()
        unitApi.initialiseUnits()
        playerApi.initialisePlayers()
        if (tiles.isNotEmpty()) battlefieldApi.initialiseBattlefield()
        eventBus.dispatch()
        battleUnitApi.initialiseBattleUnits()
        eventBus.dispatch()
        if (playerOrder.isNotEmpty()) battleApi.initialiseBattle()
        eventBus.dispatch()
    }
}
