package com.mkz.tessavar.battleUnit.adapters.presentation

import com.mkz.tessavar.ability.adapters.presentation.AbilityApi
import com.mkz.tessavar.battleUnit.adapters.events.OnAbilityCasted
import com.mkz.tessavar.battleUnit.adapters.events.OnBattleFinished
import com.mkz.tessavar.battleUnit.adapters.events.OnBattleUnitDefeated
import com.mkz.tessavar.battleUnit.adapters.events.OnBattleUnitDeployed
import com.mkz.tessavar.battleUnit.adapters.events.OnBattleUnitVanished
import com.mkz.tessavar.battleUnit.adapters.events.OnPlayerTurnFinished
import com.mkz.tessavar.battleUnit.adapters.events.OnPlayerTurnStarted
import com.mkz.tessavar.battleUnit.adapters.events.OnRequestApplyEffect
import com.mkz.tessavar.battleUnit.adapters.events.OnRequestCastAbility
import com.mkz.tessavar.battleUnit.adapters.events.OnRequestDeployBattleUnit
import com.mkz.tessavar.battleUnit.adapters.events.OnRequestInitialiseBattleUnits
import com.mkz.tessavar.battleUnit.adapters.events.OnRequestRecoverBattleUnits
import com.mkz.tessavar.battleUnit.adapters.events.OnRequestRemoveEquippedEffects
import com.mkz.tessavar.battleUnit.adapters.events.OnRequestRespawnBattleUnit
import com.mkz.tessavar.battleUnit.adapters.events.OnRequestSwapBattleUnits
import com.mkz.tessavar.battleUnit.adapters.events.OnRequestWalkBattleUnitInBattle
import com.mkz.tessavar.battleUnit.adapters.events.OnRequestWalkBattleUnitOutOfBattle
import com.mkz.tessavar.battleUnit.adapters.events.OnTerrainEffectApplied
import com.mkz.tessavar.battleUnit.adapters.save.BattleUnitSnapshottable
import com.mkz.tessavar.battleUnit.adapters.storage.InMemoryBattleUnitRepository
import com.mkz.tessavar.battleUnit.domain.BattleUnitRepository
import com.mkz.tessavar.battleUnit.usecases.commands.ApplyEffect
import com.mkz.tessavar.battleUnit.usecases.commands.CastAbility
import com.mkz.tessavar.battleUnit.usecases.commands.DeployBattleUnit
import com.mkz.tessavar.battleUnit.usecases.commands.InitialiseBattleUnits
import com.mkz.tessavar.battleUnit.usecases.commands.LoadBattleUnitsSnapshot
import com.mkz.tessavar.battleUnit.usecases.commands.RecoverBattleUnits
import com.mkz.tessavar.battleUnit.usecases.commands.ReduceRemainingSummonDurations
import com.mkz.tessavar.battleUnit.usecases.commands.RemoveEquippedEffects
import com.mkz.tessavar.battleUnit.usecases.commands.ReplenishMana
import com.mkz.tessavar.battleUnit.usecases.commands.RequestEffectApplicationToCastTargets
import com.mkz.tessavar.battleUnit.usecases.commands.RequestJoinBattle
import com.mkz.tessavar.battleUnit.usecases.commands.RequestLeaveBattle
import com.mkz.tessavar.battleUnit.usecases.commands.ResetBattleUnitActionsAndReduceCooldowns
import com.mkz.tessavar.battleUnit.usecases.commands.RespawnBattleUnit
import com.mkz.tessavar.battleUnit.usecases.commands.SwapBattleUnits
import com.mkz.tessavar.battleUnit.usecases.commands.TriggerOnDefeatedEffects
import com.mkz.tessavar.battleUnit.usecases.commands.TriggerOnPlayerTurnStartedEffects
import com.mkz.tessavar.battleUnit.usecases.commands.VanishSummons
import com.mkz.tessavar.battleUnit.usecases.commands.WalkBattleUnitInBattle
import com.mkz.tessavar.battleUnit.usecases.commands.WalkBattleUnitOutOfBattle
import com.mkz.tessavar.battleUnit.usecases.queries.CanCastAbility
import com.mkz.tessavar.battleUnit.usecases.queries.CanLearnAbility
import com.mkz.tessavar.battleUnit.usecases.queries.CanWalkTo
import com.mkz.tessavar.battleUnit.usecases.queries.PreviewCastOngoingEffects
import com.mkz.tessavar.battleUnit.usecases.queries.PreviewCastRemainingHealth
import com.mkz.tessavar.battleUnit.usecases.queries.SearchAbilityAvailabilities
import com.mkz.tessavar.battleUnit.usecases.queries.SearchBattleUnitById
import com.mkz.tessavar.battleUnit.usecases.queries.SearchBattleUnitsByPlayerId
import com.mkz.tessavar.battleUnit.usecases.queries.SearchBattleUnitsSnapshot
import com.mkz.tessavar.battleUnit.usecases.queries.WhereCanCast
import com.mkz.tessavar.battleUnit.usecases.queries.WhereCanWalk
import com.mkz.tessavar.battleUnit.usecases.services.AdjacencyService
import com.mkz.tessavar.battleUnit.usecases.services.BattleUnitDeployment
import com.mkz.tessavar.battleUnit.usecases.services.BattleUnitLoader
import com.mkz.tessavar.battleUnit.usecases.services.CastResolution
import com.mkz.tessavar.battlefield.adapters.presentation.BattlefieldApi
import com.mkz.tessavar.effect.adapters.presentation.EffectApi
import com.mkz.tessavar.player.adapters.presentation.PlayerApi
import com.mkz.tessavar.shared.domain.EventBus
import com.mkz.tessavar.shared.domain.Snapshottable
import com.mkz.tessavar.unit.adapters.presentation.UnitApi

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
    val requestLeaveBattle = RequestLeaveBattle(eventBus)

    private val onPlayerTurnStarted = OnPlayerTurnStarted(resetBattleUnitActionsAndReduceCooldowns, triggerOnPlayerTurnStartedEffects, replenishMana, eventBus)
    private val onPlayerTurnFinished = OnPlayerTurnFinished(reduceRemainingSummonDurations, eventBus)
    private val onBattleFinished = OnBattleFinished(vanishSummons, eventBus)
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
