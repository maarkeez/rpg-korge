package battlefield.domain

interface BattlefieldPublisher {
    fun publish(events: Set<BattlefieldEvent>)
}
