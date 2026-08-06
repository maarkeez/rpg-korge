package battle.usecases.commands

import battle.domain.Battle
import battle.domain.BattlePublisher
import battle.domain.BattleRepository

class FinishPlayerTurn(
    private val battleRepository: BattleRepository,
    private val battlePublisher: BattlePublisher,
) {

    operator fun invoke() {
        val storedBattle = battleRepository.search() ?: return
        val (events, battle) = storedBattle.finishPlayerTurn().pullEvents()
        battleRepository.update(battle)
        battlePublisher.publish(events)
    }
}
