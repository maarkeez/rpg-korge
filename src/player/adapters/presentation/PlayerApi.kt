package player.adapters.presentation

import player.adapters.events.InMemoryPlayerPublisher
import player.adapters.storage.InMemoryPlayerRepository
import player.domain.PlayerPublisher
import player.domain.PlayerRepository
import player.usecases.commands.RequestPlayerCreation
import player.usecases.queries.SearchPlayerById

class PlayerApi {
    private val playerRepository : PlayerRepository = InMemoryPlayerRepository()
    private val playerPublisher : PlayerPublisher = InMemoryPlayerPublisher()
    val requestPlayerCreation = RequestPlayerCreation(playerRepository, playerPublisher)
    val searchPlayerById = SearchPlayerById(playerRepository)
}
