package battle.usecases.commands

import battle.domain.*
import shared.domain.*

class FinishPlayerTurn(
    private val battleRepository: BattleRepository,
    private val eventBus: EventBus,
) {

    operator fun invoke() {
        val storedBattle = battleRepository.search() ?: return
        val (events, battle) = storedBattle.finishPlayerTurn().pullEvents()
        battleRepository.update(battle)
        eventBus.publish(events)
    }
}
