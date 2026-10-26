package com.mkz.rpg.player.usecases.commands

import com.mkz.rpg.player.adapters.storage.InMemoryPlayerRepository
import com.mkz.rpg.player.domain.Player
import com.mkz.rpg.player.domain.PlayerEvent
import com.mkz.rpg.player.domain.PlayerMother.id
import com.mkz.rpg.player.domain.PlayerMother.player
import com.mkz.rpg.shared.domain.FakeEventBus
import com.mkz.rpg.shared.domain.shouldHavePublishedEvents
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class RequestPlayerCreationTest {
    private val playerRepository = InMemoryPlayerRepository()
    private val eventBus = FakeEventBus()
    private val requestPlayerCreation =
        RequestPlayerCreation(
            playerRepository = playerRepository,
            eventBus = eventBus,
        )

    @Test
    fun `should create human player when player type is human`() {
        // Given
        val playerId = id()
        // When
        requestPlayerCreation(id = playerId, type = RequestPlayerCreation.PlayerType.HUMAN)
        // Then
        val storedPlayer = playerRepository.searchById(playerId)?.toDto()
        storedPlayer shouldBe player(playerId, type = Player.Dto.PlayerTypeDto.HUMAN).toDto()
        eventBus.shouldHavePublishedEvents(PlayerEvent.PlayerCreated(playerId, Player.Dto.PlayerTypeDto.HUMAN))
    }

    @Test
    fun `should create cpu player when player type is cpu`() {
        // Given
        val playerId = id()
        // When
        requestPlayerCreation(id = playerId, type = RequestPlayerCreation.PlayerType.CPU)
        // Then
        val storedPlayer = playerRepository.searchById(playerId)?.toDto()
        storedPlayer shouldBe player(playerId, type = Player.Dto.PlayerTypeDto.CPU).toDto()
        eventBus.shouldHavePublishedEvents(PlayerEvent.PlayerCreated(playerId, Player.Dto.PlayerTypeDto.CPU))
    }

    @Test
    fun `should not create player when it already exists`() {
        // Given
        val existingPlayer = player()
        playerRepository.create(existingPlayer)
        // When
        requestPlayerCreation(id = existingPlayer.toDto().id, type = RequestPlayerCreation.PlayerType.HUMAN)
        // Then
        val storedPlayer = playerRepository.searchById(existingPlayer.toDto().id)?.toDto()
        storedPlayer shouldBe existingPlayer.toDto()
        eventBus.shouldHavePublishedEvents()
    }
}
