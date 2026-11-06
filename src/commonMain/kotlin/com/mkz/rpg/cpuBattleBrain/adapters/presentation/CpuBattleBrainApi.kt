package com.mkz.rpg.cpuBattleBrain.adapters.presentation

import com.mkz.rpg.ability.adapters.presentation.AbilityApi
import com.mkz.rpg.battleUnit.adapters.presentation.BattleUnitApi
import com.mkz.rpg.battlefield.adapters.presentation.BattlefieldApi
import com.mkz.rpg.cpuBattleBrain.adapters.events.OnAbilityCastResolved
import com.mkz.rpg.cpuBattleBrain.adapters.events.OnBattleDisengaged
import com.mkz.rpg.cpuBattleBrain.adapters.events.OnBattleUnitDeployed
import com.mkz.rpg.cpuBattleBrain.adapters.events.OnCpuBattleUnitDecisionResolved
import com.mkz.rpg.cpuBattleBrain.adapters.events.OnCpuBattleUnitDone
import com.mkz.rpg.cpuBattleBrain.adapters.events.OnCpuPlayerTurnStarted
import com.mkz.rpg.cpuBattleBrain.adapters.events.OnExplorationStarted
import com.mkz.rpg.cpuBattleBrain.adapters.events.OnPartyWalked
import com.mkz.rpg.cpuBattleBrain.adapters.events.OnPlayerTurnStarted
import com.mkz.rpg.cpuBattleBrain.adapters.events.OnRequestEngageEnemies
import com.mkz.rpg.cpuBattleBrain.adapters.events.OnRequestJoinAndLeaveBattle
import com.mkz.rpg.cpuBattleBrain.adapters.events.OnTileOccupied
import com.mkz.rpg.cpuBattleBrain.adapters.storage.InMemoryCpuBattleBrainRepository
import com.mkz.rpg.cpuBattleBrain.domain.CpuBattleBrainRepository
import com.mkz.rpg.cpuBattleBrain.usecases.commands.EngageEnemies
import com.mkz.rpg.cpuBattleBrain.usecases.commands.JoinAndLeaveBattle
import com.mkz.rpg.cpuBattleBrain.usecases.commands.JoinCpuPlayerTurn
import com.mkz.rpg.cpuBattleBrain.usecases.commands.JoinDisengagedBattle
import com.mkz.rpg.cpuBattleBrain.usecases.commands.MakeNextDecision
import com.mkz.rpg.cpuBattleBrain.usecases.commands.PlayPlayerTurn
import com.mkz.rpg.cpuBattleBrain.usecases.commands.ResolveCpuBattleUnitCast
import com.mkz.rpg.cpuBattleBrain.usecases.commands.ResolveCpuBattleUnitWalk
import com.mkz.rpg.cpuBattleBrain.usecases.queries.HealingNeed
import com.mkz.rpg.cpuBattleBrain.usecases.queries.IsOutsideTerritory
import com.mkz.rpg.cpuBattleBrain.usecases.queries.SearchDistancesFromHome
import com.mkz.rpg.cpuBattleBrain.usecases.queries.SearchEnemiesWithinReach
import com.mkz.rpg.cpuBattleBrain.usecases.queries.SearchTerrainHealthEffect
import com.mkz.rpg.cpuBattleBrain.usecases.queries.WhatShouldCast
import com.mkz.rpg.cpuBattleBrain.usecases.queries.WhereShouldWalk
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
    random: Random,
) {
    // Queries
    val healingNeed = HealingNeed(battleUnitApi.searchBattleUnitById)
    val searchTerrainHealthEffect = SearchTerrainHealthEffect(battlefieldApi.searchTerrainId, terrainApi.searchTerrainById, effectApi.searchEffectById)
    val searchDistancesFromHome = SearchDistancesFromHome(battlefieldApi.isTileOccupiable, searchTerrainHealthEffect)
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

    val whereShouldWalk =
        WhereShouldWalk(
            battleUnitApi.searchBattleUnitsByPlayerId,
            battleUnitApi.whereCanWalk,
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
            battlefieldApi.isTileVacant,
            healingNeed,
            searchTerrainHealthEffect,
            random,
        )

    // Storage
    private val cpuBattleBrainRepository: CpuBattleBrainRepository = InMemoryCpuBattleBrainRepository()

    // Commands
    val playPlayerTurn =
        PlayPlayerTurn(
            playerApi.searchPlayerById,
            battleUnitApi.searchBattleUnitsByPlayerId,
            cpuBattleBrainRepository,
            eventBus,
        )
    val makeNextDecision =
        MakeNextDecision(
            cpuBattleBrainRepository,
            battleUnitApi.searchBattleUnitsByPlayerId,
            whatShouldCast,
            whereShouldWalk,
            eventBus,
        )
    val engageEnemies =
        EngageEnemies(
            playerApi.searchEnemyPlayer,
            battleUnitApi.searchBattleUnitsByPlayerId,
            searchEnemiesWithinReach,
            eventBus,
        )
    val joinAndLeaveBattle =
        JoinAndLeaveBattle(
            playerApi.searchHumanPlayer,
            playerApi.searchEnemyPlayer,
            battleUnitApi.searchBattleUnitsByPlayerId,
            searchEnemiesWithinReach,
            eventBus,
        )
    val joinDisengagedBattle =
        JoinDisengagedBattle(
            playerApi.searchHumanPlayer,
            playerApi.searchEnemyPlayer,
            battleUnitApi.searchBattleUnitsByPlayerId,
            searchEnemiesWithinReach,
            eventBus,
        )
    val resolveCpuBattleUnitWalk = ResolveCpuBattleUnitWalk(cpuBattleBrainRepository, eventBus)
    val resolveCpuBattleUnitCast = ResolveCpuBattleUnitCast(cpuBattleBrainRepository, eventBus)
    val joinCpuPlayerTurn = JoinCpuPlayerTurn(cpuBattleBrainRepository, battleUnitApi.searchBattleUnitById)

    // Events
    val onPlayerTurnStarted = OnPlayerTurnStarted(eventBus, playPlayerTurn)
    private val onCpuPlayerTurnStarted = OnCpuPlayerTurnStarted(makeNextDecision, eventBus)
    private val onCpuBattleUnitDecisionResolved = OnCpuBattleUnitDecisionResolved(makeNextDecision, eventBus)
    private val onCpuBattleUnitDone = OnCpuBattleUnitDone(makeNextDecision, eventBus)
    private val onTileOccupied = OnTileOccupied(resolveCpuBattleUnitWalk, eventBus)
    private val onAbilityCastResolved = OnAbilityCastResolved(resolveCpuBattleUnitCast, eventBus)
    private val onBattleUnitDeployed = OnBattleUnitDeployed(joinCpuPlayerTurn, eventBus)
    private val onExplorationStarted = OnExplorationStarted(engageEnemies, eventBus)
    private val onPartyWalked = OnPartyWalked(engageEnemies, eventBus)
    private val onRequestEngageEnemies = OnRequestEngageEnemies(engageEnemies, eventBus)
    private val onRequestJoinAndLeaveBattle = OnRequestJoinAndLeaveBattle(joinAndLeaveBattle, eventBus)
    private val onBattleDisengaged = OnBattleDisengaged(joinDisengagedBattle, eventBus)
}
