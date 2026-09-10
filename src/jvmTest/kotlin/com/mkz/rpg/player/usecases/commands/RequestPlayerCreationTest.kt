package com.mkz.rpg.player.usecases.commands

import com.mkz.rpg.player.adapters.storage.InMemoryPlayerRepository
import com.mkz.rpg.player.domain.Player
import com.mkz.rpg.player.domain.PlayerEvent
import com.mkz.rpg.player.domain.PlayerMother.id
import com.mkz.rpg.player.domain.PlayerMother.name
import com.mkz.rpg.player.domain.PlayerMother.player
import com.mkz.rpg.shared.domain.FakeEventBus
import com.mkz.rpg.shared.domain.assertThat
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

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
        val playerId =
            id()
        val playerName =
            name()
        // When
        requestPlayerCreation(id = playerId, name = playerName, type = RequestPlayerCreation.PlayerType.HUMAN)
        // Then
        val storedPlayer = playerRepository.searchById(playerId)?.toDto()
        assertThat(storedPlayer).isEqualTo(
            player(playerId, playerName, type = Player.Dto.PlayerTypeDto.HUMAN)
                .toDto(),
        )
        assertThat(eventBus)
            .hasPublishedEvents(PlayerEvent.PlayerCreated(playerId, playerName, Player.Dto.PlayerTypeDto.HUMAN))
    }

    @Test
    fun `should create cpu player when player type is cpu`() {
        // Given
        val playerId = id()
        val playerName = name()
        // When
        requestPlayerCreation(id = playerId, name = playerName, type = RequestPlayerCreation.PlayerType.CPU)
        // Then
        val storedPlayer = playerRepository.searchById(playerId)?.toDto()
        assertThat(storedPlayer).isEqualTo(
            player(playerId, playerName, type = Player.Dto.PlayerTypeDto.CPU).toDto(),
        )
        assertThat(eventBus)
            .hasPublishedEvents(PlayerEvent.PlayerCreated(playerId, playerName, Player.Dto.PlayerTypeDto.CPU))
    }

    @Test
    fun `should not create player when it already exists`() {
        // Given
        val existingPlayer =
            player()
        playerRepository.create(existingPlayer)
        // When
        requestPlayerCreation(
            id = existingPlayer.toDto().id,
            name = existingPlayer.toDto().name,
            type = RequestPlayerCreation.PlayerType.HUMAN,
        )
        // Then
        val storedPlayer = playerRepository.searchById(existingPlayer.toDto().id)?.toDto()
        assertThat(storedPlayer).isEqualTo(existingPlayer.toDto())
        assertThat(eventBus).hasPublishedEvents()
    }
}
