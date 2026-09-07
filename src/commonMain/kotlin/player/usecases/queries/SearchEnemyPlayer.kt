package player.usecases.queries

import player.domain.Player
import player.domain.PlayerRepository

class SearchEnemyPlayer(
    private val playerRepository: PlayerRepository,
) {
    operator fun invoke(playerId: String): Player.Dto? = playerRepository.searchEnemy(playerId).toDto()
}
