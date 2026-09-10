package com.mkz.rpg.player.adapters.presentation

import com.mkz.rpg.player.adapters.storage.InMemoryPlayerRepository
import com.mkz.rpg.player.domain.PlayerRepository
import com.mkz.rpg.player.usecases.commands.RequestPlayerCreation
import com.mkz.rpg.player.usecases.queries.SearchEnemyPlayer
import com.mkz.rpg.player.usecases.queries.SearchPlayerById
import com.mkz.rpg.shared.domain.EventBus

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
