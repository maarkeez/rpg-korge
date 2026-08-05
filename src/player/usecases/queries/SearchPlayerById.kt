package player.usecases.queries

import player.domain.Player
import player.domain.PlayerPublisher
import player.domain.PlayerRepository

class SearchPlayerById(
    private val playerRepository: PlayerRepository,
) {
    operator fun invoke(id: String): Player.PlayerDto? = playerRepository.searchById(id)?.toDto()
}
