package player.adapters.presentation

import player.adapters.storage.*
import player.domain.*
import player.usecases.commands.*
import player.usecases.queries.*
import shared.domain.*

class PlayerApi(eventBus: EventBus) {
    // Storage
    private val playerRepository : PlayerRepository = InMemoryPlayerRepository()

    // Commands
    val requestPlayerCreation = RequestPlayerCreation(playerRepository, eventBus)

    // Queries
    val searchPlayerById = SearchPlayerById(playerRepository)
    val searchEnemyPlayer = SearchEnemyPlayer(playerRepository)
}
