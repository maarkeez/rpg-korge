package ability.domain

// TODO: delete
interface AbilityPublisher {
    fun publish(events: Set<AbilityEvent>)
}
