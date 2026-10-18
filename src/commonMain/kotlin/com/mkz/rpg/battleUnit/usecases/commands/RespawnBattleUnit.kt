package com.mkz.rpg.battleUnit.usecases.commands

import com.mkz.rpg.battleUnit.domain.BattleUnitRepository
import com.mkz.rpg.battlefield.usecases.queries.CanBattlefieldTileBeOccupied
import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.unit.usecases.queries.SearchUnitById

/**
 * Brings a defeated battle unit back on the tile, as it was deployed. Nothing happens when it is not defeated or the
 * tile can not be occupied (e.g.: another battle unit stands on it), so it can be requested again later.
 */
class RespawnBattleUnit(
    private val battleUnitRepository: BattleUnitRepository,
    private val searchUnitById: SearchUnitById,
    private val canBattlefieldTileBeOccupied: CanBattlefieldTileBeOccupied,
    private val eventBus: EventBus,
) {
    operator fun invoke(
        battleUnitId: String,
        row: Int,
        column: Int,
    ) {
        val storedBattleUnit = battleUnitRepository.searchById(battleUnitId) ?: return
        if (!storedBattleUnit.isDefeated() || !canBattlefieldTileBeOccupied(row, column)) return
        val unit = searchUnitById(storedBattleUnit.toDto().unitId) ?: return
        val (events, battleUnit) = storedBattleUnit.respawn(unit, row, column).pullEvents()
        battleUnitRepository.update(battleUnit)
        eventBus.publish(events)
    }
}
