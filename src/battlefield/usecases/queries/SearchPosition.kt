package battlefield.usecases.queries

import battlefield.domain.Battlefield.Dto.PositionDto
import battlefield.domain.BattlefieldRepository
import kotlin.collections.emptyList

class SearchPosition(private val battlefieldRepository: BattlefieldRepository) {
    operator fun invoke(battleUnitId: String): PositionDto? {
        val battlefield = battlefieldRepository.search() ?: return null
        return battlefield.position(battleUnitId)
    }
}
