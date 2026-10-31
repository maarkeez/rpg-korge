package com.mkz.rpg.battleUnit.usecases.queries

import com.mkz.rpg.battleUnit.domain.BattleUnit
import com.mkz.rpg.battleUnit.domain.BattleUnitRepository

/** What is saved of every battle unit, the defeated ones left out, as they are deleted. */
class SearchBattleUnitsSnapshot(
    private val battleUnitRepository: BattleUnitRepository,
) {
    operator fun invoke(): List<BattleUnit.Dto.SnapshotDto> = battleUnitRepository.searchAll().map(BattleUnit::toSnapshot)
}
