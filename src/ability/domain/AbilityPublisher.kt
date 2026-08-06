package ability.domain

interface AbilityPublisher {
    fun publish(events: Set<AbilityEvent>)
}
