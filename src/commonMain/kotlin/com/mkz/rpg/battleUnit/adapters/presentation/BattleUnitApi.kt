package com.mkz.rpg.battleUnit.adapters.presentation

import com.mkz.rpg.ability.adapters.presentation.AbilityApi
import com.mkz.rpg.battleUnit.adapters.events.OnAbilityCasted
import com.mkz.rpg.battleUnit.adapters.events.OnBattleFinished
import com.mkz.rpg.battleUnit.adapters.events.OnBattleUnitDefeated
import com.mkz.rpg.battleUnit.adapters.events.OnBattleUnitDeployed
import com.mkz.rpg.battleUnit.adapters.events.OnBattleUnitVanished
import com.mkz.rpg.battleUnit.adapters.events.OnPlayerTurnFinished
import com.mkz.rpg.battleUnit.adapters.events.OnPlayerTurnStarted
import com.mkz.rpg.battleUnit.adapters.events.OnRequestApplyEffect
import com.mkz.rpg.battleUnit.adapters.events.OnRequestCastAbility
import com.mkz.rpg.battleUnit.adapters.events.OnRequestDeployBattleUnit
import com.mkz.rpg.battleUnit.adapters.events.OnRequestInitialiseBattleUnits
import com.mkz.rpg.battleUnit.adapters.events.OnRequestMoveBattleUnit
import com.mkz.rpg.battleUnit.adapters.events.OnRequestRecoverBattleUnits
import com.mkz.rpg.battleUnit.adapters.events.OnRequestRemoveEquippedEffects
import com.mkz.rpg.battleUnit.adapters.events.OnRequestRespawnBattleUnit
import com.mkz.rpg.battleUnit.adapters.events.OnRequestSwapBattleUnits
import com.mkz.rpg.battleUnit.adapters.events.OnRequestWalkBattleUnit
import com.mkz.rpg.battleUnit.adapters.events.OnTerrainEffectTriggered
import com.mkz.rpg.battleUnit.adapters.resources.ResourcesBattleUnitLoader
import com.mkz.rpg.battleUnit.adapters.storage.InMemoryBattleUnitRepository
import com.mkz.rpg.battleUnit.domain.BattleUnitRepository
import com.mkz.rpg.battleUnit.usecases.commands.ApplyEffect
import com.mkz.rpg.battleUnit.usecases.commands.ApplyOnDefeatedEffectsToNearbyAllies
import com.mkz.rpg.battleUnit.usecases.commands.ApplyOnTurnStartedEffects
import com.mkz.rpg.battleUnit.usecases.commands.CastAbility
import com.mkz.rpg.battleUnit.usecases.commands.DeployBattleUnit
import com.mkz.rpg.battleUnit.usecases.commands.FinishSummonedBattleUnitsTurn
import com.mkz.rpg.battleUnit.usecases.commands.InitialiseBattleUnits
import com.mkz.rpg.battleUnit.usecases.commands.MoveBattleUnit
import com.mkz.rpg.battleUnit.usecases.commands.RecoverBattleUnits
import com.mkz.rpg.battleUnit.usecases.commands.RemoveEquippedEffects
import com.mkz.rpg.battleUnit.usecases.commands.ReplenishMana
import com.mkz.rpg.battleUnit.usecases.commands.RequestBattleJoin
import com.mkz.rpg.battleUnit.usecases.commands.RequestBattleLeave
import com.mkz.rpg.battleUnit.usecases.commands.RequestEffectApplicationToCastTargets
import com.mkz.rpg.battleUnit.usecases.commands.ResetBattleUnitActionsAndReduceCooldowns
import com.mkz.rpg.battleUnit.usecases.commands.RespawnBattleUnit
import com.mkz.rpg.battleUnit.usecases.commands.SwapBattleUnits
import com.mkz.rpg.battleUnit.usecases.commands.VanishSummonedBattleUnits
import com.mkz.rpg.battleUnit.usecases.commands.WalkBattleUnit
import com.mkz.rpg.battleUnit.usecases.queries.CanCastAbility
import com.mkz.rpg.battleUnit.usecases.queries.CanLearnAbility
import com.mkz.rpg.battleUnit.usecases.queries.CanMoveTo
import com.mkz.rpg.battleUnit.usecases.queries.HasAllBattleUnitsDefeated
import com.mkz.rpg.battleUnit.usecases.queries.PreviewCastOngoingEffects
import com.mkz.rpg.battleUnit.usecases.queries.PreviewCastRemainingHealth
import com.mkz.rpg.battleUnit.usecases.queries.SearchAbilityAvailabilities
import com.mkz.rpg.battleUnit.usecases.queries.SearchBattleUnitById
import com.mkz.rpg.battleUnit.usecases.queries.SearchBattleUnitsByPlayerId
import com.mkz.rpg.battleUnit.usecases.queries.WhereCanCast
import com.mkz.rpg.battleUnit.usecases.queries.WhereCanMove
import com.mkz.rpg.battleUnit.usecases.services.AbilityExecution
import com.mkz.rpg.battleUnit.usecases.services.BattleUnitDeployment
import com.mkz.rpg.battleUnit.usecases.services.BattleUnitLoader
import com.mkz.rpg.battleUnit.usecases.services.DistanceService
import com.mkz.rpg.battlefield.adapters.presentation.BattlefieldApi
import com.mkz.rpg.effect.adapters.presentation.EffectApi
import com.mkz.rpg.player.adapters.presentation.PlayerApi
import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.unit.adapters.presentation.UnitApi

class BattleUnitApi(
    effectApi: EffectApi,
    abilityApi: AbilityApi,
    unitApi: UnitApi,
    playerApi: PlayerApi,
    battlefieldApi: BattlefieldApi,
    eventBus: EventBus,
    private val battleUnitLoader: BattleUnitLoader = ResourcesBattleUnitLoader(),
) {
    // Storage
    private val battleUnitRepository: BattleUnitRepository = InMemoryBattleUnitRepository()

    // Services
    private val distanceService = DistanceService()
    private val battleUnitDeployment =
        BattleUnitDeployment(
            battleUnitRepository,
            unitApi.searchUnitById,
            playerApi.searchPlayerById,
            battlefieldApi.canBattlefieldTileBeOccupied,
        )

    // Queries
    val searchBattleUnitById = SearchBattleUnitById(battleUnitRepository)
    val canMoveTo =
        CanMoveTo(
            battleUnitRepository,
            battlefieldApi.searchPosition,
            battlefieldApi.searchShortestPath,
        )
    val whereCanMove =
        WhereCanMove(
            battleUnitRepository,
            battlefieldApi.searchTilesThatCanBeOccupied,
            canMoveTo,
        )
    val whereCanCast =
        WhereCanCast(
            battleUnitRepository,
            battlefieldApi.searchPosition,
            abilityApi.searchAbilityById,
            battlefieldApi.searchOccupant,
            battlefieldApi.searchTilesThatCanBeOccupied,
            distanceService,
            battlefieldApi.canBattlefieldTileBeOccupied,
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

    // Services
    val abilityExecution =
        AbilityExecution(
            battleUnitRepository,
            battlefieldApi.searchPosition,
            battlefieldApi.searchOccupant,
            effectApi.searchEffectById,
            distanceService,
        )
    val previewCastOngoingEffects =
        PreviewCastOngoingEffects(
            abilityApi.searchAbilityById,
            effectApi.searchEffectById,
            abilityExecution,
        )
    val previewCastRemainingHealth =
        PreviewCastRemainingHealth(
            abilityApi.searchAbilityById,
            effectApi.searchEffectById,
            battleUnitRepository,
            battlefieldApi.searchPosition,
            abilityExecution,
        )

    // Commands
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
    val moveBattleUnit =
        MoveBattleUnit(
            battleUnitRepository,
            eventBus,
            battlefieldApi.searchPosition,
            battlefieldApi.searchShortestPath,
        )
    val walkBattleUnit =
        WalkBattleUnit(
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
    val applyOnDefeatedEffectsToNearbyAllies =
        ApplyOnDefeatedEffectsToNearbyAllies(
            battleUnitRepository,
            effectApi.searchEffectById,
            battlefieldApi.searchOccupant,
            distanceService,
            eventBus,
        )
    val requestEffectApplicationToCastTargets =
        RequestEffectApplicationToCastTargets(
            abilityApi.searchAbilityById,
            battleUnitRepository,
            abilityExecution,
            eventBus,
        )
    val hasAllBattleUnitsDefeated = HasAllBattleUnitsDefeated(battleUnitRepository)
    val searchBattleUnitsByPlayerId = SearchBattleUnitsByPlayerId(battleUnitRepository)
    val applyOnTurnStartedEffects =
        ApplyOnTurnStartedEffects(
            effectApi.searchEffectById,
            searchBattleUnitsByPlayerId,
            battleUnitRepository,
            battlefieldApi.searchPosition,
            eventBus,
        )
    val replenishMana = ReplenishMana(battleUnitRepository)
    val recoverBattleUnits = RecoverBattleUnits(battleUnitRepository)
    val respawnBattleUnit = RespawnBattleUnit(battleUnitRepository, unitApi.searchUnitById, battlefieldApi.canBattlefieldTileBeOccupied, eventBus)
    val removeEquippedEffects = RemoveEquippedEffects(battleUnitRepository, eventBus)
    val finishSummonedBattleUnitsTurn = FinishSummonedBattleUnitsTurn(battleUnitRepository, battlefieldApi.searchPosition, eventBus)
    val vanishSummonedBattleUnits = VanishSummonedBattleUnits(battleUnitRepository, battlefieldApi.searchPosition, eventBus)
    val requestBattleJoin = RequestBattleJoin(eventBus)
    val requestBattleLeave = RequestBattleLeave(hasAllBattleUnitsDefeated, eventBus)

    // Events
    private val onPlayerTurnStarted = OnPlayerTurnStarted(resetBattleUnitActionsAndReduceCooldowns, applyOnTurnStartedEffects, replenishMana, eventBus)
    private val onPlayerTurnFinished = OnPlayerTurnFinished(finishSummonedBattleUnitsTurn, eventBus)
    private val onBattleFinished = OnBattleFinished(vanishSummonedBattleUnits, eventBus)
    private val onAbilityCasted = OnAbilityCasted(requestEffectApplicationToCastTargets, eventBus)
    private val onBattleUnitDeployed = OnBattleUnitDeployed(requestBattleJoin, eventBus)
    private val onBattleUnitDefeated = OnBattleUnitDefeated(applyOnDefeatedEffectsToNearbyAllies, requestBattleLeave, eventBus)
    private val onBattleUnitVanished = OnBattleUnitVanished(requestBattleLeave, eventBus)
    private val onRequestCastAbility = OnRequestCastAbility(castAbility, eventBus)
    private val onRequestMoveBattleUnit = OnRequestMoveBattleUnit(moveBattleUnit, eventBus)
    private val onRequestWalkBattleUnit = OnRequestWalkBattleUnit(walkBattleUnit, eventBus)
    private val onRequestSwapBattleUnits = OnRequestSwapBattleUnits(swapBattleUnits, eventBus)
    private val onRequestRecoverBattleUnits = OnRequestRecoverBattleUnits(recoverBattleUnits, eventBus)
    private val onRequestRespawnBattleUnit = OnRequestRespawnBattleUnit(respawnBattleUnit, eventBus)
    private val onRequestDeployBattleUnit = OnRequestDeployBattleUnit(deployBattleUnit, eventBus)
    private val onRequestInitialiseBattleUnits = OnRequestInitialiseBattleUnits(initialiseBattleUnits, eventBus)
    private val onRequestApplyEffect = OnRequestApplyEffect(applyEffect, eventBus)
    private val onRequestRemoveEquippedEffects = OnRequestRemoveEquippedEffects(removeEquippedEffects, eventBus)
    private val onTerrainEffectTriggered = OnTerrainEffectTriggered(applyEffect, eventBus)

    suspend fun init() {
        battleUnitLoader.initResources()
    }
}
