package player.adapters.events

import player.domain.PlayerEvent
import player.domain.PlayerPublisher

class InMemoryPlayerPublisher : PlayerPublisher {
    override fun publish(events: Set<PlayerEvent>) {
        
    }
}
