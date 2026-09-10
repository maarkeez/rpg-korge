package com.mkz.rpg.battleUnit.usecases.queries

import com.mkz.rpg.battleUnit.domain.BattleUnit
import com.mkz.rpg.battleUnit.domain.BattleUnitRepository

class SearchBattleUnitById(
    private val battleUnitRepository: BattleUnitRepository,
) {
    operator fun invoke(id: String): BattleUnit.Dto? = battleUnitRepository.searchById(id)?.toDto()
}
