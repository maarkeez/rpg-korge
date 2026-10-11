package com.mkz.rpg.battleUnit.usecases.services

import com.mkz.rpg.battleUnit.domain.BattleUnit
import com.mkz.rpg.battleUnit.domain.BattleUnitError.BattlefieldTileCanNotBeOccupied
import com.mkz.rpg.battleUnit.domain.BattleUnitError.PlayerNotFound
import com.mkz.rpg.battleUnit.domain.BattleUnitError.UnitNotFound
import com.mkz.rpg.battleUnit.domain.BattleUnitEvent
import com.mkz.rpg.battleUnit.domain.BattleUnitRepository
import com.mkz.rpg.battlefield.usecases.queries.CanBattlefieldTileBeOccupied
import com.mkz.rpg.player.usecases.queries.SearchPlayerById
import com.mkz.rpg.unit.usecases.queries.SearchUnitById

/** Deploys a battle unit on the battlefield once its unit, its player and the tile are valid. Returns the events to publish. */
class BattleUnitDeployment(
    private val battleUnitRepository: BattleUnitRepository,
    private val searchUnitById: SearchUnitById,
    private val searchPlayerById: SearchPlayerById,
    private val canBattlefieldTileBeOccupied: CanBattlefieldTileBeOccupied,
) {
    fun deploy(
        battleUnitId: String,
        unitId: String,
        playerId: String,
        deployAtRow: Int,
        deployAtColumn: Int,
    ): Set<BattleUnitEvent> {
        val unit = searchUnitById(unitId) ?: throw UnitNotFound()
        val player = searchPlayerById(playerId) ?: throw PlayerNotFound()
        if (!canBattlefieldTileBeOccupied(deployAtRow, deployAtColumn)) throw BattlefieldTileCanNotBeOccupied()
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
        return events
    }
}
