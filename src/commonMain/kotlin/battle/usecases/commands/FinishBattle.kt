package battle.usecases.commands

import battle.domain.*
import shared.domain.*

class FinishBattle(
    private val battleRepository: BattleRepository,
    private val eventBus: EventBus,
) {

    operator fun invoke() {
        val storedBattle = battleRepository.search() ?: return
        if(!storedBattle.isBattleFinished()) return
        val (events, battle) = storedBattle.finishBattle().pullEvents()
        battleRepository.update(battle)
        eventBus.publish(events)
    }
}
