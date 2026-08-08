package battleunit.adapters.presentation

import battlefield.adapters.presentation.BattlefieldApi
import battleunit.adapters.storage.InMemoryBattleUnitRepository
import battleunit.domain.BattleUnitRepository
import battleunit.usecases.commands.DeployBattleUnit
import battleunit.usecases.queries.SearchBattleUnitById
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

    // Commands
    val deployBattleUnit = DeployBattleUnit(
        battleUnitRepository,
        eventBus,
        unitApi.searchUnitById,
        playerApi.searchPlayerById,
        battlefieldApi.canBattlefieldTileBeOccupied
    )

    // Queries
    val searchBattleUnitById = SearchBattleUnitById(battleUnitRepository)

}
