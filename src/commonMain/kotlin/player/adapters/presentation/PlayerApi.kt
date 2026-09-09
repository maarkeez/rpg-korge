package player.adapters.presentation

import player.adapters.storage.InMemoryPlayerRepository
import player.domain.PlayerRepository
import player.usecases.commands.RequestPlayerCreation
import player.usecases.queries.SearchEnemyPlayer
import player.usecases.queries.SearchPlayerById
import shared.domain.EventBus

class PlayerApi(
    eventBus: EventBus,
) {
    // Storage
    private val playerRepository: PlayerRepository = InMemoryPlayerRepository()

    // Commands
    val requestPlayerCreation = RequestPlayerCreation(playerRepository, eventBus)

    // Queries
    val searchPlayerById = SearchPlayerById(playerRepository)
    val searchEnemyPlayer = SearchEnemyPlayer(playerRepository)
}
