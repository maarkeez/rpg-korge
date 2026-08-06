package battle.usecases.commands

import battle.domain.Battle
import battle.domain.BattlePublisher
import battle.domain.BattleRepository
import shared.domain.EventBus

class FinishBattle(
    private val battleRepository: BattleRepository,
    private val eventBus: EventBus,
) {

    operator fun invoke() {
        val storedBattle = battleRepository.search() ?: return
        val (events, battle) = storedBattle.finishBattle().pullEvents()
        battleRepository.update(battle)
        eventBus.publish(events)
    }
}
