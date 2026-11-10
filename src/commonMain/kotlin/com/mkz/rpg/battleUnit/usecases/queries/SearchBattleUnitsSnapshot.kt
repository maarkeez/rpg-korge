package com.mkz.rpg.battleUnit.usecases.queries

import com.mkz.rpg.battleUnit.domain.BattleUnit
import com.mkz.rpg.battleUnit.domain.BattleUnitRepository

class SearchBattleUnitsSnapshot(
    private val battleUnitRepository: BattleUnitRepository,
) {
    operator fun invoke(): List<BattleUnit.Dto.SnapshotDto> = battleUnitRepository.searchAll().map(BattleUnit::toSnapshot)
}
