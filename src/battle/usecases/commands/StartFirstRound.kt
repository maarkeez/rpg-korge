package battle.usecases.commands

import battle.domain.Battle
import battle.domain.BattlePublisher
import battle.domain.BattleRepository

class StartFirstRound(
    private val battleRepository: BattleRepository,
    private val battlePublisher: BattlePublisher,
) {

    operator fun invoke(players: List<String>) {
        val (events, battle) = Battle.startFirstRound(players).pullEvents()
        battleRepository.create(battle)
        battlePublisher.publish(events)
    }
}
