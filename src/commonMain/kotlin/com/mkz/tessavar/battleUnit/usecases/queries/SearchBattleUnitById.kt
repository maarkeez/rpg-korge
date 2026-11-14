package com.mkz.tessavar.battleUnit.usecases.queries

import com.mkz.tessavar.battleUnit.domain.BattleUnit
import com.mkz.tessavar.battleUnit.domain.BattleUnitRepository

class SearchBattleUnitById(
    private val battleUnitRepository: BattleUnitRepository,
) {
    operator fun invoke(id: String): BattleUnit.Dto? = battleUnitRepository.searchById(id)?.toDto()
}
