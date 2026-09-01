package battleunit.usecases.commands

import battleunit.domain.*
import unit.usecases.queries.SearchUnitById

class ReplenishMana(
    private val battleUnitRepository: BattleUnitRepository,
    private val searchUnitById: SearchUnitById,
) {
    operator fun invoke(
        playerId: String,
    ) {
        battleUnitRepository.searchByPlayerId(playerId).map {  battleUnit ->
            val unit = searchUnitById(battleUnit.toDto().unitId)!!
            battleUnit.replenishMana(unit)
        }.forEach(battleUnitRepository::update)
    }
}
