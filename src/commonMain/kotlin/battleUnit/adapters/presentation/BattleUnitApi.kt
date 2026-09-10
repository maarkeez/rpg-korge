package battleUnit.adapters.presentation

import ability.adapters.presentation.AbilityApi
import battleUnit.adapters.events.OnAbilityCasted
import battleUnit.adapters.events.OnPlayerTurnStarted
import battleUnit.adapters.storage.InMemoryBattleUnitRepository
import battleUnit.domain.BattleUnitRepository
import battleUnit.usecases.commands.ApplyOnTurnStartedEffects
import battleUnit.usecases.commands.CastAbility
import battleUnit.usecases.commands.DeployBattleUnit
import battleUnit.usecases.commands.MoveBattleUnit
import battleUnit.usecases.commands.ReceiveAbilityEffects
import battleUnit.usecases.commands.ReplenishMana
import battleUnit.usecases.commands.ResetBattleUnitActionsAndReduceCooldowns
import battleUnit.usecases.queries.CanCastAbility
import battleUnit.usecases.queries.CanMoveTo
import battleUnit.usecases.queries.HasAllBattleUnitsDefeated
import battleUnit.usecases.queries.SearchBattleUnitById
import battleUnit.usecases.queries.SearchBattleUnitsByPlayerId
import battleUnit.usecases.queries.WhereCanCast
import battleUnit.usecases.queries.WhereCanMove
import battleUnit.usecases.services.DistanceService
import battlefield.adapters.presentation.BattlefieldApi
import effect.adapters.presentation.EffectApi
import player.adapters.presentation.PlayerApi
import shared.domain.EventBus
import unit.adapters.presentation.UnitApi

class BattleUnitApi(
    effectApi: EffectApi,
    abilityApi: AbilityApi,
    unitApi: UnitApi,
    playerApi: PlayerApi,
    battlefieldApi: BattlefieldApi,
    eventBus: EventBus,
) {
    // Storage
    private val battleUnitRepository: BattleUnitRepository = InMemoryBattleUnitRepository()

    // Services
    private val distanceService = DistanceService()

    // Queries
    val searchBattleUnitById = SearchBattleUnitById(battleUnitRepository)
    val canMoveTo =
        CanMoveTo(
            battleUnitRepository,
            battlefieldApi.searchPosition,
            distanceService,
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
            distanceService,
            battlefieldApi.canBattlefieldTileBeOccupied,
        )
    val canCastAbility =
        CanCastAbility(
            battleUnitRepository,
            abilityApi.searchAbilityById,
        )

    // Commands
    val deployBattleUnit =
        DeployBattleUnit(
            battleUnitRepository,
            eventBus,
            unitApi.searchUnitById,
            playerApi.searchPlayerById,
            battlefieldApi.canBattlefieldTileBeOccupied,
        )
    val moveBattleUnit =
        MoveBattleUnit(
            battleUnitRepository,
            eventBus,
            battlefieldApi.searchPosition,
            distanceService,
        )
    val resetBattleUnitActionsAndReduceCooldowns = ResetBattleUnitActionsAndReduceCooldowns(battleUnitRepository)
    val castAbility =
        CastAbility(
            whereCanCast,
            abilityApi.searchAbilityById,
            battleUnitRepository,
            eventBus,
        )
    val receiveAbilityEffects =
        ReceiveAbilityEffects(
            abilityApi.searchAbilityById,
            effectApi.searchEffectById,
            battleUnitRepository,
            eventBus,
            battlefieldApi.searchOccupant,
            unitApi.searchUnitById,
            battlefieldApi.searchPosition,
        )
    val hasAllBattleUnitsDefeated = HasAllBattleUnitsDefeated(battleUnitRepository)
    val searchBattleUnitsByPlayerId = SearchBattleUnitsByPlayerId(battleUnitRepository)
    val applyOnTurnStartedEffects =
        ApplyOnTurnStartedEffects(
            effectApi.searchEffectById,
            searchBattleUnitsByPlayerId,
            battleUnitRepository,
            eventBus,
        )
    val replenishMana =
        ReplenishMana(
            battleUnitRepository,
            unitApi.searchUnitById,
        )

    // Events
    private val onPlayerTurnStarted = OnPlayerTurnStarted(resetBattleUnitActionsAndReduceCooldowns, applyOnTurnStartedEffects, replenishMana, eventBus)
    private val onAbilityCasted = OnAbilityCasted(receiveAbilityEffects, eventBus)
}
