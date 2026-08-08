package battleunit.usecases.queries

import battleunit.domain.*

class SearchBattleUnitById(
    private val battleUnitRepository: BattleUnitRepository
) {
    operator fun invoke(id: String): BattleUnit.Dto? = battleUnitRepository.searchById(id)?.toDto()
}
