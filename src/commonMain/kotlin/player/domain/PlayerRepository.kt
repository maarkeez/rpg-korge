package player.domain

interface PlayerRepository {
    fun create(player: Player)
    fun searchById(id: String): Player?
    fun searchEnemy(playerId: String): Player
}
