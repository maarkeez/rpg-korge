package player.domain

sealed interface PlayerEvent {
    data class PlayerCreated(
        val playerId: String,
        val playerName: String,
        val playerType: String,
    ): PlayerEvent
}
