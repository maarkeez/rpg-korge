package com.mkz.rpg.cpuBattleBrain.adapters.presentation

import com.mkz.rpg.ability.adapters.presentation.AbilityApi
import com.mkz.rpg.battleUnit.adapters.presentation.BattleUnitApi
import com.mkz.rpg.battlefield.adapters.presentation.BattlefieldApi
import com.mkz.rpg.cpuBattleBrain.adapters.events.OnAbilityCastResolved
import com.mkz.rpg.cpuBattleBrain.adapters.events.OnBattleDisengaged
import com.mkz.rpg.cpuBattleBrain.adapters.events.OnBattleUnitDeployed
import com.mkz.rpg.cpuBattleBrain.adapters.events.OnBattlefieldTileOccupied
import com.mkz.rpg.cpuBattleBrain.adapters.events.OnCpuBattleBrainInitialized
import com.mkz.rpg.cpuBattleBrain.adapters.events.OnCpuBattleUnitActionResolved
import com.mkz.rpg.cpuBattleBrain.adapters.events.OnCpuBattleUnitManaged
import com.mkz.rpg.cpuBattleBrain.adapters.events.OnExplorationStarted
import com.mkz.rpg.cpuBattleBrain.adapters.events.OnPartyWalked
import com.mkz.rpg.cpuBattleBrain.adapters.events.OnPlayerTurnStarted
import com.mkz.rpg.cpuBattleBrain.adapters.events.OnRequestEngageEnemies
import com.mkz.rpg.cpuBattleBrain.adapters.events.OnRequestUpdateEngagement
import com.mkz.rpg.cpuBattleBrain.adapters.storage.InMemoryCpuBattleBrainRepository
import com.mkz.rpg.cpuBattleBrain.domain.CpuBattleBrainRepository
import com.mkz.rpg.cpuBattleBrain.usecases.commands.EngageEnemies
import com.mkz.rpg.cpuBattleBrain.usecases.commands.JoinCpuTurn
import com.mkz.rpg.cpuBattleBrain.usecases.commands.ManageCpuBattleUnit
import com.mkz.rpg.cpuBattleBrain.usecases.commands.PlayTurn
import com.mkz.rpg.cpuBattleBrain.usecases.commands.ReengageEnemies
import com.mkz.rpg.cpuBattleBrain.usecases.commands.ResolveCpuBattleUnitCast
import com.mkz.rpg.cpuBattleBrain.usecases.commands.ResolveCpuBattleUnitMove
import com.mkz.rpg.cpuBattleBrain.usecases.commands.UpdateEngagement
import com.mkz.rpg.cpuBattleBrain.usecases.queries.HealingNeed
import com.mkz.rpg.cpuBattleBrain.usecases.queries.IsOutsideTerritory
import com.mkz.rpg.cpuBattleBrain.usecases.queries.SearchDistancesFromHome
import com.mkz.rpg.cpuBattleBrain.usecases.queries.SearchEnemiesWithinReach
import com.mkz.rpg.cpuBattleBrain.usecases.queries.SearchTerrainHealthEffect
import com.mkz.rpg.cpuBattleBrain.usecases.queries.WhatShouldCast
import com.mkz.rpg.cpuBattleBrain.usecases.queries.WhereShouldMove
import com.mkz.rpg.cpuBattleBrain.usecases.queries.WhereShouldWander
import com.mkz.rpg.effect.adapters.presentation.EffectApi
import com.mkz.rpg.player.adapters.presentation.PlayerApi
import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.terrain.adapters.presentation.TerrainApi
import com.mkz.rpg.unit.adapters.presentation.UnitApi
import kotlin.random.Random

class CpuBattleBrainApi(
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
    val searchTerrainHealthEffect = SearchTerrainHealthEffect(battlefieldApi.searchTerrainId, terrainApi.searchTerrainById, effectApi.searchEffectById)
    val searchDistancesFromHome = SearchDistancesFromHome(battlefieldApi.canBattlefieldTerrainBeOccupied, searchTerrainHealthEffect)
    val isOutsideTerritory = IsOutsideTerritory(battlefieldApi.searchPosition)

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
            healingNeed,
            searchTerrainHealthEffect,
            random,
        )

    // Storage
    private val cpuBattleBrainRepository: CpuBattleBrainRepository = InMemoryCpuBattleBrainRepository()

    // Commands
    val playTurn =
        PlayTurn(
            playerApi.searchPlayerById,
            battleUnitApi.searchBattleUnitsByPlayerId,
            cpuBattleBrainRepository,
            eventBus,
        )
    val manageCpuBattleUnit =
        ManageCpuBattleUnit(
            cpuBattleBrainRepository,
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
    val reengageEnemies =
        ReengageEnemies(
            playerApi.searchHumanPlayer,
            playerApi.searchEnemyPlayer,
            battleUnitApi.searchBattleUnitsByPlayerId,
            searchEnemiesWithinReach,
            eventBus,
        )
    val resolveCpuBattleUnitMove = ResolveCpuBattleUnitMove(cpuBattleBrainRepository, eventBus)
    val resolveCpuBattleUnitCast = ResolveCpuBattleUnitCast(cpuBattleBrainRepository, eventBus)
    val joinCpuTurn = JoinCpuTurn(cpuBattleBrainRepository, battleUnitApi.searchBattleUnitById)

    // Events
    val onPlayerTurnStarted = OnPlayerTurnStarted(eventBus, playTurn)
    private val onCpuBattleBrainInitialized = OnCpuBattleBrainInitialized(manageCpuBattleUnit, eventBus)
    private val onCpuBattleUnitActionResolved = OnCpuBattleUnitActionResolved(manageCpuBattleUnit, eventBus)
    private val onCpuBattleUnitManaged = OnCpuBattleUnitManaged(manageCpuBattleUnit, eventBus)
    private val onBattlefieldTileOccupied = OnBattlefieldTileOccupied(resolveCpuBattleUnitMove, eventBus)
    private val onAbilityCastResolved = OnAbilityCastResolved(resolveCpuBattleUnitCast, eventBus)
    private val onBattleUnitDeployed = OnBattleUnitDeployed(joinCpuTurn, eventBus)
    private val onExplorationStarted = OnExplorationStarted(engageEnemies, eventBus)
    private val onPartyWalked = OnPartyWalked(engageEnemies, eventBus)
    private val onRequestEngageEnemies = OnRequestEngageEnemies(engageEnemies, eventBus)
    private val onRequestUpdateEngagement = OnRequestUpdateEngagement(updateEngagement, eventBus)
    private val onBattleDisengaged = OnBattleDisengaged(reengageEnemies, eventBus)
}
