package player.domain

interface PlayerPublisher {
    fun publish(events: Set<PlayerEvent>)
}
