package com.mkz.tessavar.battleUnit.usecases.commands

import com.mkz.tessavar.battleUnit.domain.BattleUnitRepository
import com.mkz.tessavar.battlefield.usecases.queries.IsTileVacant
import com.mkz.tessavar.shared.domain.EventBus
import com.mkz.tessavar.unit.usecases.queries.SearchUnitById

class RespawnBattleUnit(
    private val battleUnitRepository: BattleUnitRepository,
    private val searchUnitById: SearchUnitById,
    private val isTileVacant: IsTileVacant,
    private val eventBus: EventBus,
) {
    operator fun invoke(
        battleUnitId: String,
        row: Int,
        column: Int,
    ) {
        val storedBattleUnit = battleUnitRepository.searchById(battleUnitId) ?: return
        if (!storedBattleUnit.isDefeated() || !isTileVacant(row, column)) return
        val unit = searchUnitById(storedBattleUnit.toDto().unitId) ?: return
        val (events, battleUnit) = storedBattleUnit.respawn(unit, row, column).pullEvents()
        battleUnitRepository.update(battleUnit)
        eventBus.publish(events)
    }
}
