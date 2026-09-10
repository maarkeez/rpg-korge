package battleUnit.usecases.queries

import battleUnit.domain.BattleUnit
import battleUnit.domain.BattleUnitRepository

class SearchBattleUnitById(
    private val battleUnitRepository: BattleUnitRepository,
) {
    operator fun invoke(id: String): BattleUnit.Dto? = battleUnitRepository.searchById(id)?.toDto()
}
