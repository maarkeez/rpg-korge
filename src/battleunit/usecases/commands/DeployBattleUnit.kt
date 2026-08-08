package battleunit.usecases.commands

import battlefield.usecases.queries.CanBattlefieldTileBeOccupied
import battleunit.domain.BattleUnit
import battleunit.domain.BattleUnitError
import battleunit.domain.BattleUnitError.BattlefieldTileCanNotBeOccupied
import battleunit.domain.BattleUnitError.PlayerNotFound
import battleunit.domain.BattleUnitError.UnitNotFound
import battleunit.domain.BattleUnitRepository
import player.usecases.queries.SearchPlayerById
import shared.domain.EventBus
import unit.usecases.queries.SearchUnitById

class DeployBattleUnit(
    private val battleUnitRepository: BattleUnitRepository,
    private val eventBus: EventBus,
    private val searchUnitById: SearchUnitById,
    private val searchPlayerById: SearchPlayerById,
    private val canBattlefieldTileBeOccupied: CanBattlefieldTileBeOccupied,
) {
    operator fun invoke(
        battleUnitId: String,
        unitId: String,
        playerId: String,
        deployAtRow: Int,
        deployAtColumn: Int,
    ) {
        val unit = searchUnitById(unitId) ?: throw UnitNotFound()
        val player = searchPlayerById(playerId) ?: throw PlayerNotFound()
        val canBattlefieldTileBeOccupied = canBattlefieldTileBeOccupied(deployAtRow, deployAtColumn)
        if(!canBattlefieldTileBeOccupied) throw BattlefieldTileCanNotBeOccupied()
        val (events, battleUnit) = BattleUnit.deploy(
            id = battleUnitId,
            unit = unit,
            player = player,
            deployAtRow = deployAtRow,
            deployAtColumn = deployAtColumn,
        ).pullEvents()
        battleUnitRepository.create(battleUnit)
        eventBus.publish(events)
    }
}
