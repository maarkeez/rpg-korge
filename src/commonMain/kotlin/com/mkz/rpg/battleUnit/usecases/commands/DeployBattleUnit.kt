package com.mkz.rpg.battleUnit.usecases.commands

import com.mkz.rpg.battleUnit.domain.BattleUnit
import com.mkz.rpg.battleUnit.domain.BattleUnitError.BattlefieldTileCanNotBeOccupied
import com.mkz.rpg.battleUnit.domain.BattleUnitError.PlayerNotFound
import com.mkz.rpg.battleUnit.domain.BattleUnitError.UnitNotFound
import com.mkz.rpg.battleUnit.domain.BattleUnitRepository
import com.mkz.rpg.battlefield.usecases.queries.CanBattlefieldTileBeOccupied
import com.mkz.rpg.player.usecases.queries.SearchPlayerById
import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.unit.usecases.queries.SearchUnitById

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
        if (!canBattlefieldTileBeOccupied) throw BattlefieldTileCanNotBeOccupied()
        val (events, battleUnit) =
            BattleUnit
                .deploy(
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
