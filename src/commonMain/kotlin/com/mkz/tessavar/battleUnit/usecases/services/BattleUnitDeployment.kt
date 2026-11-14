package com.mkz.tessavar.battleUnit.usecases.services

import com.mkz.tessavar.battleUnit.domain.BattleUnit
import com.mkz.tessavar.battleUnit.domain.BattleUnitError.PlayerNotFound
import com.mkz.tessavar.battleUnit.domain.BattleUnitError.TileIsNotVacant
import com.mkz.tessavar.battleUnit.domain.BattleUnitError.UnitNotFound
import com.mkz.tessavar.battleUnit.domain.BattleUnitRepository
import com.mkz.tessavar.battlefield.usecases.queries.IsTileVacant
import com.mkz.tessavar.player.usecases.queries.SearchPlayerById
import com.mkz.tessavar.shared.domain.DomainEvent
import com.mkz.tessavar.unit.usecases.queries.SearchMaximumAbilities
import com.mkz.tessavar.unit.usecases.queries.SearchUnitById

class BattleUnitDeployment(
    private val battleUnitRepository: BattleUnitRepository,
    private val searchUnitById: SearchUnitById,
    private val searchMaximumAbilities: SearchMaximumAbilities,
    private val searchPlayerById: SearchPlayerById,
    private val isTileVacant: IsTileVacant,
) {
    fun deploy(
        battleUnitId: String,
        unitId: String,
        playerId: String,
        deployAtRow: Int,
        deployAtColumn: Int,
        remainingSummonDuration: Int? = null,
    ): Set<DomainEvent> {
        val unit = searchUnitById(unitId) ?: throw UnitNotFound()
        val player = searchPlayerById(playerId) ?: throw PlayerNotFound()
        if (!isTileVacant(deployAtRow, deployAtColumn)) throw TileIsNotVacant()
        val (events, battleUnit) =
            BattleUnit
                .deploy(
                    id = battleUnitId,
                    unit = unit,
                    player = player,
                    maximumAbilities = searchMaximumAbilities(),
                    deployAtRow = deployAtRow,
                    deployAtColumn = deployAtColumn,
                    remainingSummonDuration = remainingSummonDuration,
                ).pullEvents()
        battleUnitRepository.create(battleUnit)
        return events
    }
}
