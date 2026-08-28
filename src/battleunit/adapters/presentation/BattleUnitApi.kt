package battleunit.adapters.presentation

import ability.adapters.presentation.AbilityApi
import battlefield.adapters.presentation.BattlefieldApi
import battleunit.adapters.events.OnAbilityCasted
import battleunit.adapters.events.OnPlayerTurnStarted
import battleunit.adapters.storage.InMemoryBattleUnitRepository
import battleunit.domain.BattleUnitRepository
import battleunit.usecases.commands.CastAbility
import battleunit.usecases.commands.DeployBattleUnit
import battleunit.usecases.commands.MoveBattleUnit
import battleunit.usecases.commands.ReceiveAbilityEffects
import battleunit.usecases.commands.ResetBattleUnitActionsAndReduceCooldowns
import battleunit.usecases.queries.CanCastAbility
import battleunit.usecases.queries.CanMoveTo
import battleunit.usecases.queries.HasAllBattleUnitsDefeated
import battleunit.usecases.queries.SearchBattleUnitById
import battleunit.usecases.queries.SearchBattleUnitsByPlayerId
import battleunit.usecases.queries.WhereCanCast
import battleunit.usecases.queries.WhereCanMove
import battleunit.usecases.services.DistanceService
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
){

    // Storage
    private val battleUnitRepository: BattleUnitRepository = InMemoryBattleUnitRepository()

    // Services
    private val distanceService = DistanceService()

    // Queries
    val searchBattleUnitById = SearchBattleUnitById(battleUnitRepository)
    val canMoveTo = CanMoveTo(
        battleUnitRepository,
        battlefieldApi.searchPosition,
        distanceService
    )
    val whereCanMove = WhereCanMove(
        battleUnitRepository,
        battlefieldApi.searchTilesThatCanBeOccupied,
        canMoveTo,
    )
    val whereCanCast = WhereCanCast(
        battleUnitRepository,
        battlefieldApi.searchPosition,
        abilityApi.searchAbilityById,
        battlefieldApi.searchOccupant,
        distanceService,
    )
    val canCastAbility = CanCastAbility(
        battleUnitRepository,
    )

    // Commands
    val deployBattleUnit = DeployBattleUnit(
        battleUnitRepository,
        eventBus,
        unitApi.searchUnitById,
        playerApi.searchPlayerById,
        battlefieldApi.canBattlefieldTileBeOccupied
    )
    val moveBattleUnit = MoveBattleUnit(
        battleUnitRepository,
        eventBus,
        battlefieldApi.searchPosition,
        distanceService,
    )
    val resetBattleUnitActionsAndReduceCooldowns = ResetBattleUnitActionsAndReduceCooldowns(battleUnitRepository)
    val castAbility = CastAbility(
        whereCanCast,
        abilityApi.searchAbilityById,
        battleUnitRepository,
        eventBus,
    )
    val receiveAbilityEffects = ReceiveAbilityEffects(
        abilityApi.searchAbilityById,
        effectApi.searchEffectById,
        battleUnitRepository,
        eventBus,
        battlefieldApi.searchOccupant,
        unitApi.searchUnitById,
    )
    val hasAllBattleUnitsDefeated = HasAllBattleUnitsDefeated(battleUnitRepository)
    val searchBattleUnitsByPlayerId = SearchBattleUnitsByPlayerId(battleUnitRepository)

    // Events
    private val onPlayerTurnStarted = OnPlayerTurnStarted(resetBattleUnitActionsAndReduceCooldowns, eventBus)
    private val onAbilityCasted = OnAbilityCasted(receiveAbilityEffects, eventBus)
}
