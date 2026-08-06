package battle.usecases.commands

import battle.domain.Battle
import battle.domain.BattlePublisher
import battle.domain.BattleRepository

class DefeatPlayer(
    private val battleRepository: BattleRepository,
    private val battlePublisher: BattlePublisher,
) {

    operator fun invoke(playerId: String) {
        val storedBattle = battleRepository.search() ?: return
        val (events, battle) = storedBattle.defeatPlayer(playerId).pullEvents()
        battleRepository.update(battle)
        battlePublisher.publish(events)
    }
}
