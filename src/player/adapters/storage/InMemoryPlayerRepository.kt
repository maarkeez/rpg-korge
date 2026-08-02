package player.adapters.storage

import player.domain.Player
import player.domain.PlayerRepository

class InMemoryPlayerRepository: PlayerRepository {
    private val players = mutableMapOf<String, Player>()
    override fun create(player: Player) {
        players[player.toDto().id] = player
    }

    override fun searchById(id: String) = players[id]
}
