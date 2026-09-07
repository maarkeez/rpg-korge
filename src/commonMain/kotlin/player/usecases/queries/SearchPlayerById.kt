package player.usecases.queries

import player.domain.Player
import player.domain.PlayerRepository

class SearchPlayerById(
    private val playerRepository: PlayerRepository,
) {
    operator fun invoke(id: String): Player.Dto? = playerRepository.searchById(id)?.toDto()
}
