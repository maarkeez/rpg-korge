package com.mkz.rpg.cpuBrain.adapters.presentation

import com.mkz.rpg.ability.adapters.presentation.AbilityApi
import com.mkz.rpg.battleUnit.adapters.presentation.BattleUnitApi
import com.mkz.rpg.battlefield.adapters.presentation.BattlefieldApi
import com.mkz.rpg.cpuBrain.adapters.events.OnAbilityCastResolved
import com.mkz.rpg.cpuBrain.adapters.events.OnBattleUnitsWanderedAfterRound
import com.mkz.rpg.cpuBrain.adapters.events.OnBattleUnitsWanderedWhileExploring
import com.mkz.rpg.cpuBrain.adapters.events.OnBattlefieldTileOccupied
import com.mkz.rpg.cpuBrain.adapters.events.OnCpuBattleUnitActionResolved
import com.mkz.rpg.cpuBrain.adapters.events.OnCpuBattleUnitManaged
import com.mkz.rpg.cpuBrain.adapters.events.OnCpuBrainInitialized
import com.mkz.rpg.cpuBrain.adapters.events.OnExplorationStarted
import com.mkz.rpg.cpuBrain.adapters.events.OnPartyWalked
import com.mkz.rpg.cpuBrain.adapters.events.OnPlayerTurnStarted
import com.mkz.rpg.cpuBrain.adapters.storage.InMemoryCpuBrainRepository
import com.mkz.rpg.cpuBrain.domain.CpuBrainRepository
import com.mkz.rpg.cpuBrain.usecases.commands.EngageEnemies
import com.mkz.rpg.cpuBrain.usecases.commands.ManageCpuBattleUnit
import com.mkz.rpg.cpuBrain.usecases.commands.PlayTurn
import com.mkz.rpg.cpuBrain.usecases.commands.ResolveCpuBattleUnitCast
import com.mkz.rpg.cpuBrain.usecases.commands.ResolveCpuBattleUnitMove
import com.mkz.rpg.cpuBrain.usecases.commands.UpdateEngagement
import com.mkz.rpg.cpuBrain.usecases.queries.HealingNeed
import com.mkz.rpg.cpuBrain.usecases.queries.SearchEnemiesWithinReach
import com.mkz.rpg.cpuBrain.usecases.queries.SearchTerrainHealthEffect
import com.mkz.rpg.cpuBrain.usecases.queries.WhatShouldCast
import com.mkz.rpg.cpuBrain.usecases.queries.WhereShouldMove
import com.mkz.rpg.cpuBrain.usecases.queries.WhereShouldWander
import com.mkz.rpg.effect.adapters.presentation.EffectApi
import com.mkz.rpg.player.adapters.presentation.PlayerApi
import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.terrain.adapters.presentation.TerrainApi
import com.mkz.rpg.unit.adapters.presentation.UnitApi
import kotlin.random.Random

class CpuBrainApi(
    unitApi: UnitApi,
    playerApi: PlayerApi,
    battleUnitApi: BattleUnitApi,
    battlefieldApi: BattlefieldApi,
    terrainApi: TerrainApi,
    effectApi: EffectApi,
    abilityApi: AbilityApi,
    eventBus: EventBus,
    random: Random = Random.Default,
) {
    // Queries
    val healingNeed = HealingNeed(battleUnitApi.searchBattleUnitById)
    val searchTerrainHealthEffect = SearchTerrainHealthEffect(battlefieldApi.searchBattlefield, terrainApi.searchTerrainById, effectApi.searchEffectById)

    val searchEnemiesWithinReach =
        SearchEnemiesWithinReach(
            battleUnitApi.searchBattleUnitById,
            playerApi.searchEnemyPlayer,
            battleUnitApi.searchBattleUnitsByPlayerId,
            battlefieldApi.searchPosition,
            battlefieldApi.searchWalkingDistance,
        )

    val whatShouldCast =
        WhatShouldCast(
            battleUnitApi.searchBattleUnitById,
            unitApi.searchUnitById,
            battleUnitApi.canCastAbility,
            battleUnitApi.whereCanCast,
            abilityApi.searchAbilityById,
            effectApi.searchEffectById,
            battlefieldApi.searchOccupant,
            battlefieldApi.searchPosition,
            searchEnemiesWithinReach,
            healingNeed,
            random,
        )

    val whereShouldMove =
        WhereShouldMove(
            battleUnitApi.searchBattleUnitsByPlayerId,
            battleUnitApi.whereCanMove,
            battleUnitApi.searchBattleUnitById,
            battlefieldApi.searchPosition,
            searchEnemiesWithinReach,
            healingNeed,
            searchTerrainHealthEffect,
        )

    val whereShouldWander =
        WhereShouldWander(
            battleUnitApi.searchBattleUnitById,
            battlefieldApi.searchPosition,
            battlefieldApi.canBattlefieldTileBeOccupied,
            battlefieldApi.searchWalkingDistance,
            healingNeed,
            searchTerrainHealthEffect,
            random,
        )

    // Storage
    private val cpuBrainRepository: CpuBrainRepository = InMemoryCpuBrainRepository()

    // Commands
    val playTurn =
        PlayTurn(
            playerApi.searchPlayerById,
            battleUnitApi.searchBattleUnitsByPlayerId,
            cpuBrainRepository,
            eventBus,
        )
    val manageCpuBattleUnit =
        ManageCpuBattleUnit(
            cpuBrainRepository,
            battleUnitApi.searchBattleUnitsByPlayerId,
            whatShouldCast,
            whereShouldMove,
            eventBus,
        )
    val engageEnemies =
        EngageEnemies(
            playerApi.searchEnemyPlayer,
            battleUnitApi.searchBattleUnitsByPlayerId,
            searchEnemiesWithinReach,
            eventBus,
        )
    val updateEngagement =
        UpdateEngagement(
            playerApi.searchHumanPlayer,
            playerApi.searchEnemyPlayer,
            battleUnitApi.searchBattleUnitsByPlayerId,
            searchEnemiesWithinReach,
            eventBus,
        )
    val resolveCpuBattleUnitMove = ResolveCpuBattleUnitMove(cpuBrainRepository, eventBus)
    val resolveCpuBattleUnitCast = ResolveCpuBattleUnitCast(cpuBrainRepository, eventBus)

    // Events
    val onPlayerTurnStarted = OnPlayerTurnStarted(eventBus, playTurn)
    private val onCpuBrainInitialized = OnCpuBrainInitialized(manageCpuBattleUnit, eventBus)
    private val onCpuBattleUnitActionResolved = OnCpuBattleUnitActionResolved(manageCpuBattleUnit, eventBus)
    private val onCpuBattleUnitManaged = OnCpuBattleUnitManaged(manageCpuBattleUnit, eventBus)
    private val onBattlefieldTileOccupied = OnBattlefieldTileOccupied(resolveCpuBattleUnitMove, eventBus)
    private val onAbilityCastResolved = OnAbilityCastResolved(resolveCpuBattleUnitCast, eventBus)
    private val onExplorationStarted = OnExplorationStarted(engageEnemies, eventBus)
    private val onPartyWalked = OnPartyWalked(engageEnemies, eventBus)
    private val onBattleUnitsWanderedWhileExploring = OnBattleUnitsWanderedWhileExploring(engageEnemies, eventBus)
    private val onBattleUnitsWanderedAfterRound = OnBattleUnitsWanderedAfterRound(updateEngagement, eventBus)
}
