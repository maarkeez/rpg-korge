package com.mkz.rpg.battleUnit.usecases.services

import com.mkz.rpg.battleUnit.domain.BattleUnit
import com.mkz.rpg.battleUnit.domain.BattleUnitError.BattlefieldTileCanNotBeOccupied
import com.mkz.rpg.battleUnit.domain.BattleUnitError.PlayerNotFound
import com.mkz.rpg.battleUnit.domain.BattleUnitError.UnitNotFound
import com.mkz.rpg.battleUnit.domain.BattleUnitRepository
import com.mkz.rpg.battlefield.usecases.queries.CanBattlefieldTileBeOccupied
import com.mkz.rpg.player.usecases.queries.SearchPlayerById
import com.mkz.rpg.shared.domain.DomainEvent
import com.mkz.rpg.unit.usecases.queries.SearchMaximumAbilities
import com.mkz.rpg.unit.usecases.queries.SearchUnitById

/**
 * Deploys a battle unit on the battlefield once its unit, its player and the tile are valid. Remaining turns: those a
 * summon lasts, none for the battle units that never vanish. Returns the events to publish.
 */
class BattleUnitDeployment(
    private val battleUnitRepository: BattleUnitRepository,
    private val searchUnitById: SearchUnitById,
    private val searchMaximumAbilities: SearchMaximumAbilities,
    private val searchPlayerById: SearchPlayerById,
    private val canBattlefieldTileBeOccupied: CanBattlefieldTileBeOccupied,
) {
    fun deploy(
        battleUnitId: String,
        unitId: String,
        playerId: String,
        deployAtRow: Int,
        deployAtColumn: Int,
        remainingTurns: Int? = null,
    ): Set<DomainEvent> {
        val unit = searchUnitById(unitId) ?: throw UnitNotFound()
        val player = searchPlayerById(playerId) ?: throw PlayerNotFound()
        if (!canBattlefieldTileBeOccupied(deployAtRow, deployAtColumn)) throw BattlefieldTileCanNotBeOccupied()
        val (events, battleUnit) =
            BattleUnit
                .deploy(
                    id = battleUnitId,
                    unit = unit,
                    player = player,
                    maximumAbilities = searchMaximumAbilities(),
                    deployAtRow = deployAtRow,
                    deployAtColumn = deployAtColumn,
                    remainingTurns = remainingTurns,
                ).pullEvents()
        battleUnitRepository.create(battleUnit)
        return events
    }
}
