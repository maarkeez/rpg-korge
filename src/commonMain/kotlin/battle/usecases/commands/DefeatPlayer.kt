package battle.usecases.commands

import battle.domain.BattleRepository
import battleunit.usecases.queries.HasAllBattleUnitsDefeated
import shared.domain.EventBus

class DefeatPlayer(
    private val hasAllBattleUnitsDefeated: HasAllBattleUnitsDefeated,
    private val battleRepository: BattleRepository,
    private val eventBus: EventBus,
) {
    operator fun invoke(playerId: String) {
        val storedBattle = battleRepository.search() ?: return
        if (!hasAllBattleUnitsDefeated(playerId)) return
        val (events, battle) = storedBattle.defeatPlayer(playerId).pullEvents()
        battleRepository.update(battle)
        eventBus.publish(events)
    }
}
