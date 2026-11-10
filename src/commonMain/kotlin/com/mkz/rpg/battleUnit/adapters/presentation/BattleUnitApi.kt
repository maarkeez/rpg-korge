package com.mkz.rpg.battleUnit.adapters.presentation

import com.mkz.rpg.ability.adapters.presentation.AbilityApi
import com.mkz.rpg.battleUnit.adapters.events.OnAbilityCasted
import com.mkz.rpg.battleUnit.adapters.events.OnBattleFinishedWithoutWinner
import com.mkz.rpg.battleUnit.adapters.events.OnBattleUnitDefeated
import com.mkz.rpg.battleUnit.adapters.events.OnBattleUnitDeployed
import com.mkz.rpg.battleUnit.adapters.events.OnBattleUnitVanished
import com.mkz.rpg.battleUnit.adapters.events.OnPlayerTurnFinished
import com.mkz.rpg.battleUnit.adapters.events.OnPlayerTurnStarted
import com.mkz.rpg.battleUnit.adapters.events.OnRequestApplyEffect
import com.mkz.rpg.battleUnit.adapters.events.OnRequestCastAbility
import com.mkz.rpg.battleUnit.adapters.events.OnRequestDeployBattleUnit
import com.mkz.rpg.battleUnit.adapters.events.OnRequestInitialiseBattleUnits
import com.mkz.rpg.battleUnit.adapters.events.OnRequestRecoverBattleUnits
import com.mkz.rpg.battleUnit.adapters.events.OnRequestRemoveEquippedEffects
import com.mkz.rpg.battleUnit.adapters.events.OnRequestRespawnBattleUnit
import com.mkz.rpg.battleUnit.adapters.events.OnRequestSwapBattleUnits
import com.mkz.rpg.battleUnit.adapters.events.OnRequestWalkBattleUnitInBattle
import com.mkz.rpg.battleUnit.adapters.events.OnRequestWalkBattleUnitOutOfBattle
import com.mkz.rpg.battleUnit.adapters.events.OnTerrainEffectApplied
import com.mkz.rpg.battleUnit.adapters.save.BattleUnitSnapshottable
import com.mkz.rpg.battleUnit.adapters.storage.InMemoryBattleUnitRepository
import com.mkz.rpg.battleUnit.domain.BattleUnitRepository
import com.mkz.rpg.battleUnit.usecases.commands.ApplyEffect
import com.mkz.rpg.battleUnit.usecases.commands.CastAbility
import com.mkz.rpg.battleUnit.usecases.commands.DeployBattleUnit
import com.mkz.rpg.battleUnit.usecases.commands.InitialiseBattleUnits
import com.mkz.rpg.battleUnit.usecases.commands.LoadBattleUnitsSnapshot
import com.mkz.rpg.battleUnit.usecases.commands.RecoverBattleUnits
import com.mkz.rpg.battleUnit.usecases.commands.ReduceRemainingSummonDurations
import com.mkz.rpg.battleUnit.usecases.commands.RemoveEquippedEffects
import com.mkz.rpg.battleUnit.usecases.commands.ReplenishMana
import com.mkz.rpg.battleUnit.usecases.commands.RequestEffectApplicationToCastTargets
import com.mkz.rpg.battleUnit.usecases.commands.RequestJoinBattle
import com.mkz.rpg.battleUnit.usecases.commands.RequestLeaveBattle
import com.mkz.rpg.battleUnit.usecases.commands.ResetBattleUnitActionsAndReduceCooldowns
import com.mkz.rpg.battleUnit.usecases.commands.RespawnBattleUnit
import com.mkz.rpg.battleUnit.usecases.commands.SwapBattleUnits
import com.mkz.rpg.battleUnit.usecases.commands.TriggerOnDefeatedEffects
import com.mkz.rpg.battleUnit.usecases.commands.TriggerOnPlayerTurnStartedEffects
import com.mkz.rpg.battleUnit.usecases.commands.VanishSummons
import com.mkz.rpg.battleUnit.usecases.commands.WalkBattleUnitInBattle
import com.mkz.rpg.battleUnit.usecases.commands.WalkBattleUnitOutOfBattle
import com.mkz.rpg.battleUnit.usecases.queries.AreAllBattleUnitsDefeated
import com.mkz.rpg.battleUnit.usecases.queries.CanCastAbility
import com.mkz.rpg.battleUnit.usecases.queries.CanLearnAbility
import com.mkz.rpg.battleUnit.usecases.queries.CanWalkTo
import com.mkz.rpg.battleUnit.usecases.queries.PreviewCastOngoingEffects
import com.mkz.rpg.battleUnit.usecases.queries.PreviewCastRemainingHealth
import com.mkz.rpg.battleUnit.usecases.queries.SearchAbilityAvailabilities
import com.mkz.rpg.battleUnit.usecases.queries.SearchBattleUnitById
import com.mkz.rpg.battleUnit.usecases.queries.SearchBattleUnitsByPlayerId
import com.mkz.rpg.battleUnit.usecases.queries.SearchBattleUnitsSnapshot
import com.mkz.rpg.battleUnit.usecases.queries.WhereCanCast
import com.mkz.rpg.battleUnit.usecases.queries.WhereCanWalk
import com.mkz.rpg.battleUnit.usecases.services.AdjacencyService
import com.mkz.rpg.battleUnit.usecases.services.BattleUnitDeployment
import com.mkz.rpg.battleUnit.usecases.services.BattleUnitLoader
import com.mkz.rpg.battleUnit.usecases.services.CastResolution
import com.mkz.rpg.battlefield.adapters.presentation.BattlefieldApi
import com.mkz.rpg.effect.adapters.presentation.EffectApi
import com.mkz.rpg.player.adapters.presentation.PlayerApi
import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.shared.domain.Snapshottable
import com.mkz.rpg.unit.adapters.presentation.UnitApi

class BattleUnitApi(
    effectApi: EffectApi,
    abilityApi: AbilityApi,
    unitApi: UnitApi,
    playerApi: PlayerApi,
    battlefieldApi: BattlefieldApi,
    eventBus: EventBus,
    private val battleUnitLoader: BattleUnitLoader,
) {
    private val battleUnitRepository: BattleUnitRepository = InMemoryBattleUnitRepository()

    private val searchBattleUnitsSnapshot = SearchBattleUnitsSnapshot(battleUnitRepository)
    private val loadBattleUnitsSnapshot = LoadBattleUnitsSnapshot(battleUnitRepository, unitApi.searchUnitById, playerApi.searchPlayerById, abilityApi.searchAbilityById, effectApi.searchEffectById)
    val snapshottable: Snapshottable = BattleUnitSnapshottable(searchBattleUnitsSnapshot, loadBattleUnitsSnapshot)

    private val adjacencyService = AdjacencyService()
    private val battleUnitDeployment =
        BattleUnitDeployment(
            battleUnitRepository,
            unitApi.searchUnitById,
            unitApi.searchMaximumAbilities,
            playerApi.searchPlayerById,
            battlefieldApi.isTileVacant,
        )

    val searchBattleUnitById = SearchBattleUnitById(battleUnitRepository)
    val canWalkTo =
        CanWalkTo(
            battleUnitRepository,
            battlefieldApi.searchPosition,
            battlefieldApi.searchShortestPath,
        )
    val whereCanWalk =
        WhereCanWalk(
            battleUnitRepository,
            battlefieldApi.searchVacantTiles,
            canWalkTo,
        )
    val whereCanCast =
        WhereCanCast(
            battleUnitRepository,
            battlefieldApi.searchPosition,
            abilityApi.searchAbilityById,
            battlefieldApi.searchOccupant,
            battlefieldApi.searchVacantTiles,
            adjacencyService,
            battlefieldApi.isTileVacant,
        )
    val canCastAbility =
        CanCastAbility(
            battleUnitRepository,
            abilityApi.searchAbilityById,
        )
    val searchAbilityAvailabilities =
        SearchAbilityAvailabilities(
            battleUnitRepository,
            abilityApi.searchAbilityById,
        )
    val canLearnAbility = CanLearnAbility(battleUnitRepository)

    val castResolution =
        CastResolution(
            battleUnitRepository,
            battlefieldApi.searchPosition,
            battlefieldApi.searchOccupant,
            effectApi.searchEffectById,
            adjacencyService,
        )
    val previewCastOngoingEffects =
        PreviewCastOngoingEffects(
            abilityApi.searchAbilityById,
            effectApi.searchEffectById,
            castResolution,
        )
    val previewCastRemainingHealth =
        PreviewCastRemainingHealth(
            abilityApi.searchAbilityById,
            effectApi.searchEffectById,
            battleUnitRepository,
            battlefieldApi.searchPosition,
            castResolution,
        )

    val deployBattleUnit =
        DeployBattleUnit(
            battleUnitDeployment,
            eventBus,
        )
    val initialiseBattleUnits =
        InitialiseBattleUnits(
            battleUnitLoader,
            battleUnitRepository,
            battleUnitDeployment,
            eventBus,
        )
    val walkBattleUnitInBattle =
        WalkBattleUnitInBattle(
            battleUnitRepository,
            eventBus,
            battlefieldApi.searchPosition,
            battlefieldApi.searchShortestPath,
        )
    val walkBattleUnitOutOfBattle =
        WalkBattleUnitOutOfBattle(
            battleUnitRepository,
            eventBus,
            battlefieldApi.searchPosition,
            battlefieldApi.searchShortestPath,
        )
    val swapBattleUnits = SwapBattleUnits(battleUnitRepository, eventBus, battlefieldApi.searchPosition)
    val resetBattleUnitActionsAndReduceCooldowns = ResetBattleUnitActionsAndReduceCooldowns(battleUnitRepository)
    val castAbility =
        CastAbility(
            whereCanCast,
            abilityApi.searchAbilityById,
            battleUnitRepository,
            eventBus,
        )
    val applyEffect =
        ApplyEffect(
            battleUnitRepository,
            eventBus,
            effectApi.searchEffectById,
            battlefieldApi.searchPosition,
            battleUnitDeployment,
            abilityApi.searchAbilityById,
        )
    val triggerOnDefeatedEffects =
        TriggerOnDefeatedEffects(
            battleUnitRepository,
            effectApi.searchEffectById,
            battlefieldApi.searchOccupant,
            adjacencyService,
            eventBus,
        )
    val requestEffectApplicationToCastTargets =
        RequestEffectApplicationToCastTargets(
            abilityApi.searchAbilityById,
            battleUnitRepository,
            castResolution,
            eventBus,
        )
    val areAllBattleUnitsDefeated = AreAllBattleUnitsDefeated(battleUnitRepository)
    val searchBattleUnitsByPlayerId = SearchBattleUnitsByPlayerId(battleUnitRepository)
    val triggerOnPlayerTurnStartedEffects =
        TriggerOnPlayerTurnStartedEffects(
            effectApi.searchEffectById,
            searchBattleUnitsByPlayerId,
            battleUnitRepository,
            battlefieldApi.searchPosition,
            eventBus,
        )
    val replenishMana = ReplenishMana(battleUnitRepository)
    val recoverBattleUnits = RecoverBattleUnits(battleUnitRepository)
    val respawnBattleUnit = RespawnBattleUnit(battleUnitRepository, unitApi.searchUnitById, battlefieldApi.isTileVacant, eventBus)
    val removeEquippedEffects = RemoveEquippedEffects(battleUnitRepository, eventBus)
    val reduceRemainingSummonDurations = ReduceRemainingSummonDurations(battleUnitRepository, battlefieldApi.searchPosition, eventBus)
    val vanishSummons = VanishSummons(battleUnitRepository, battlefieldApi.searchPosition, eventBus)
    val requestJoinBattle = RequestJoinBattle(eventBus)
    val requestLeaveBattle = RequestLeaveBattle(areAllBattleUnitsDefeated, eventBus)

    private val onPlayerTurnStarted = OnPlayerTurnStarted(resetBattleUnitActionsAndReduceCooldowns, triggerOnPlayerTurnStartedEffects, replenishMana, eventBus)
    private val onPlayerTurnFinished = OnPlayerTurnFinished(reduceRemainingSummonDurations, eventBus)
    private val onBattleFinishedWithoutWinner = OnBattleFinishedWithoutWinner(vanishSummons, eventBus)
    private val onAbilityCasted = OnAbilityCasted(requestEffectApplicationToCastTargets, eventBus)
    private val onBattleUnitDeployed = OnBattleUnitDeployed(requestJoinBattle, eventBus)
    private val onBattleUnitDefeated = OnBattleUnitDefeated(triggerOnDefeatedEffects, requestLeaveBattle, eventBus)
    private val onBattleUnitVanished = OnBattleUnitVanished(requestLeaveBattle, eventBus)
    private val onRequestCastAbility = OnRequestCastAbility(castAbility, eventBus)
    private val onRequestWalkBattleUnitInBattle = OnRequestWalkBattleUnitInBattle(walkBattleUnitInBattle, eventBus)
    private val onRequestWalkBattleUnitOutOfBattle = OnRequestWalkBattleUnitOutOfBattle(walkBattleUnitOutOfBattle, eventBus)
    private val onRequestSwapBattleUnits = OnRequestSwapBattleUnits(swapBattleUnits, eventBus)
    private val onRequestRecoverBattleUnits = OnRequestRecoverBattleUnits(recoverBattleUnits, eventBus)
    private val onRequestRespawnBattleUnit = OnRequestRespawnBattleUnit(respawnBattleUnit, eventBus)
    private val onRequestDeployBattleUnit = OnRequestDeployBattleUnit(deployBattleUnit, eventBus)
    private val onRequestInitialiseBattleUnits = OnRequestInitialiseBattleUnits(initialiseBattleUnits, eventBus)
    private val onRequestApplyEffect = OnRequestApplyEffect(applyEffect, eventBus)
    private val onRequestRemoveEquippedEffects = OnRequestRemoveEquippedEffects(removeEquippedEffects, eventBus)
    private val onTerrainEffectApplied = OnTerrainEffectApplied(applyEffect, eventBus)

    suspend fun init() {
        battleUnitLoader.initResources()
    }
}
