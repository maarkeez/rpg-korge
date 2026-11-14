package com.mkz.tessavar.player.adapters.presentation

import com.mkz.tessavar.player.adapters.events.OnRequestInitialisePlayers
import com.mkz.tessavar.player.adapters.events.OnRequestPlayerCreation
import com.mkz.tessavar.player.adapters.storage.InMemoryPlayerRepository
import com.mkz.tessavar.player.domain.PlayerRepository
import com.mkz.tessavar.player.usecases.commands.InitialisePlayers
import com.mkz.tessavar.player.usecases.commands.RequestPlayerCreation
import com.mkz.tessavar.player.usecases.queries.SearchHumanPlayer
import com.mkz.tessavar.player.usecases.queries.SearchOpponent
import com.mkz.tessavar.player.usecases.queries.SearchPlayerById
import com.mkz.tessavar.player.usecases.services.PlayerLoader
import com.mkz.tessavar.shared.domain.EventBus

class PlayerApi(
    eventBus: EventBus,
    private val playerLoader: PlayerLoader,
) {
    private val playerRepository: PlayerRepository = InMemoryPlayerRepository()

    val requestPlayerCreation = RequestPlayerCreation(playerRepository, eventBus)
    val initialisePlayers = InitialisePlayers(playerLoader, playerRepository, eventBus)

    val searchPlayerById = SearchPlayerById(playerRepository)
    val searchOpponent = SearchOpponent(playerRepository)
    val searchHumanPlayer = SearchHumanPlayer(playerRepository)

    private val onRequestPlayerCreation = OnRequestPlayerCreation(requestPlayerCreation, eventBus)
    private val onRequestInitialisePlayers = OnRequestInitialisePlayers(initialisePlayers, eventBus)

    suspend fun init() {
        playerLoader.initResources()
    }
}
