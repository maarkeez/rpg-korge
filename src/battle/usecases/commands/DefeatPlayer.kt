package battle.usecases.commands

import battle.domain.Battle
import battle.domain.BattlePublisher
import battle.domain.BattleRepository
import shared.domain.EventBus

class DefeatPlayer(
    private val battleRepository: BattleRepository,
    private val eventBus: EventBus,
) {

    operator fun invoke(playerId: String) {
        val storedBattle = battleRepository.search() ?: return
        val (events, battle) = storedBattle.defeatPlayer(playerId).pullEvents()
        battleRepository.update(battle)
        eventBus.publish(events)
    }
}
