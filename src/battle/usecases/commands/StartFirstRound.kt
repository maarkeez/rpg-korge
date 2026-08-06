package battle.usecases.commands

import battle.domain.Battle
import battle.domain.BattlePublisher
import battle.domain.BattleRepository
import shared.domain.EventBus

class StartFirstRound(
    private val battleRepository: BattleRepository,
    private val eventBus: EventBus,
) {

    operator fun invoke(players: List<String>) {
        val (events, battle) = Battle.startFirstRound(players).pullEvents()
        battleRepository.create(battle)
        eventBus.publish(events)
    }
}
