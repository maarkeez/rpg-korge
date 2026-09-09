package battleunit.usecases.queries

import battleunit.domain.BattleUnit
import battleunit.domain.BattleUnitRepository

class SearchBattleUnitById(
    private val battleUnitRepository: BattleUnitRepository,
) {
    operator fun invoke(id: String): BattleUnit.Dto? = battleUnitRepository.searchById(id)?.toDto()
}
