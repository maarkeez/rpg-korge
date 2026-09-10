package com.mkz.rpg.battleUnit.usecases.queries

import com.mkz.rpg.battleUnit.domain.BattleUnit
import com.mkz.rpg.battleUnit.domain.BattleUnitRepository

class SearchBattleUnitsByPlayerId(
    private val battleUnitRepository: BattleUnitRepository,
) {
    operator fun invoke(playerId: String): List<BattleUnit.Dto> =
        battleUnitRepository
            .searchByPlayerId(playerId)
            .filter { !it.isDefeated() }
            .map { it.toDto() }
}
