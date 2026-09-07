package battleunit.usecases.queries

import battleunit.domain.*

class SearchBattleUnitsByPlayerId(
    private val battleUnitRepository: BattleUnitRepository
) {
    operator fun invoke(playerId: String): List<BattleUnit.Dto> = battleUnitRepository.searchByPlayerId(playerId)
        .filter { !it.isDefeated() }
        .map { it.toDto() }
}
