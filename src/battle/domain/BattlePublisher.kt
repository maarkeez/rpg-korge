package battle.domain

import battlefield.domain.BattlefieldEvent

interface BattlePublisher {
    fun publish(events: Set<BattleEvent>)
}
