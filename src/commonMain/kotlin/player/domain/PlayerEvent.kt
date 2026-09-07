package player.domain

import shared.domain.DomainEvent

sealed interface PlayerEvent: DomainEvent {
    data class PlayerCreated(
        val playerId: String,
        val playerName: String,
        val playerType: String,
    ): PlayerEvent
}
