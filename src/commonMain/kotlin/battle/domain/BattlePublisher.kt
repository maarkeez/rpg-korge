package battle.domain

interface BattlePublisher {
    fun publish(events: Set<BattleEvent>)
}
