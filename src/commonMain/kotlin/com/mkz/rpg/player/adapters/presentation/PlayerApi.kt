package com.mkz.rpg.player.adapters.presentation

import com.mkz.rpg.player.adapters.events.OnRequestInitialisePlayers
import com.mkz.rpg.player.adapters.events.OnRequestPlayerCreation
import com.mkz.rpg.player.adapters.resources.ResourcesPlayerLoader
import com.mkz.rpg.player.adapters.storage.InMemoryPlayerRepository
import com.mkz.rpg.player.domain.PlayerRepository
import com.mkz.rpg.player.usecases.commands.InitialisePlayers
import com.mkz.rpg.player.usecases.commands.RequestPlayerCreation
import com.mkz.rpg.player.usecases.queries.SearchEnemyPlayer
import com.mkz.rpg.player.usecases.queries.SearchPlayerById
import com.mkz.rpg.player.usecases.services.PlayerLoader
import com.mkz.rpg.shared.domain.EventBus

class PlayerApi(
    eventBus: EventBus,
    private val playerLoader: PlayerLoader = ResourcesPlayerLoader(),
) {
    // Storage
    private val playerRepository: PlayerRepository = InMemoryPlayerRepository()

    // Commands
    val requestPlayerCreation = RequestPlayerCreation(playerRepository, eventBus)
    val initialisePlayers = InitialisePlayers(playerLoader, playerRepository, eventBus)

    // Queries
    val searchPlayerById = SearchPlayerById(playerRepository)
    val searchEnemyPlayer = SearchEnemyPlayer(playerRepository)

    // Events
    private val onRequestPlayerCreation = OnRequestPlayerCreation(requestPlayerCreation, eventBus)
    private val onRequestInitialisePlayers = OnRequestInitialisePlayers(initialisePlayers, eventBus)

    suspend fun init() {
        playerLoader.initResources()
    }
}
