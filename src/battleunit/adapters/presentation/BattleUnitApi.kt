package battleunit.adapters.presentation

import battlefield.adapters.presentation.BattlefieldApi
import battleunit.adapters.events.OnPlayerTurnStarted
import battleunit.adapters.storage.InMemoryBattleUnitRepository
import battleunit.domain.BattleUnitRepository
import battleunit.usecases.commands.DeployBattleUnit
import battleunit.usecases.commands.MoveBattleUnit
import battleunit.usecases.commands.ResetBattleUnitActions
import battleunit.usecases.queries.CanMoveTo
import battleunit.usecases.queries.SearchBattleUnitById
import battleunit.usecases.services.DistanceService
import player.adapters.presentation.PlayerApi
import shared.domain.EventBus
import unit.adapters.presentation.UnitApi

class BattleUnitApi(
    unitApi: UnitApi,
    playerApi: PlayerApi,
    battlefieldApi: BattlefieldApi,
    eventBus: EventBus,
){

    // Storage
    private val battleUnitRepository: BattleUnitRepository = InMemoryBattleUnitRepository()

    // Services
    private val distanceService = DistanceService()

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
    val resetBattleUnitActions = ResetBattleUnitActions(battleUnitRepository)

    // Queries
    val searchBattleUnitById = SearchBattleUnitById(battleUnitRepository)
    val canMoveTo = CanMoveTo(
        battleUnitRepository,
        battlefieldApi.searchPosition,
        distanceService
    )

    // Events
    private val onPlayerTurnStarted = OnPlayerTurnStarted(resetBattleUnitActions, eventBus)
}
