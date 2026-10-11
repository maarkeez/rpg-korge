package com.mkz.rpg.player.usecases.commands

import com.mkz.rpg.player.adapters.storage.InMemoryPlayerRepository
import com.mkz.rpg.player.domain.Player.Dto.PlayerTypeDto.CPU
import com.mkz.rpg.player.domain.Player.Dto.PlayerTypeDto.HUMAN
import com.mkz.rpg.player.domain.PlayerEvent.PlayerCreated
import com.mkz.rpg.player.domain.PlayerMother.player
import com.mkz.rpg.player.usecases.services.PlayerLoader
import com.mkz.rpg.shared.domain.FakeEventBus
import com.mkz.rpg.shared.domain.assertThat
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class InitialisePlayersTest {
    private val playerRepository = InMemoryPlayerRepository()
    private val eventBus = FakeEventBus()
    private val playerLoader: PlayerLoader = mock()
    private val initialisePlayers = InitialisePlayers(playerLoader, playerRepository, eventBus)

    @Test
    fun `should create every loaded player when players are initialised`() {
        // Given
        val human = player(id = "player-one", name = "Human", type = HUMAN).toDto()
        val cpu = player(id = "player-two", name = "CPU", type = CPU).toDto()
        whenever(playerLoader.loadPlayers()).thenReturn(listOf(human, cpu))
        // When
        initialisePlayers()
        // Then
        assertThat(playerRepository.searchById("player-one")?.toDto()).isEqualTo(human)
        assertThat(playerRepository.searchById("player-two")?.toDto()).isEqualTo(cpu)
        assertThat(eventBus).hasPublishedEvents(
            PlayerCreated(playerId = "player-one", playerName = "Human", playerType = HUMAN),
            PlayerCreated(playerId = "player-two", playerName = "CPU", playerType = CPU),
        )
    }

    @Test
    fun `should not create a player again when the player already exists`() {
        // Given
        val human = player(id = "player-one", name = "Human", type = HUMAN)
        playerRepository.create(human)
        whenever(playerLoader.loadPlayers()).thenReturn(listOf(player(id = "player-one", name = "Other", type = CPU).toDto()))
        // When
        initialisePlayers()
        // Then
        assertThat(playerRepository.searchById("player-one")?.toDto()).isEqualTo(human.toDto())
        assertThat(eventBus).hasNotPublishedEvents()
    }
}
