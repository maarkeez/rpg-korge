package battleUnit.usecases.queries

import battleUnit.domain.BattleUnit
import battleUnit.domain.BattleUnitRepository

class SearchBattleUnitsByPlayerId(
    private val battleUnitRepository: BattleUnitRepository,
) {
    operator fun invoke(playerId: String): List<BattleUnit.Dto> =
        battleUnitRepository
            .searchByPlayerId(playerId)
            .filter { !it.isDefeated() }
            .map { it.toDto() }
}
