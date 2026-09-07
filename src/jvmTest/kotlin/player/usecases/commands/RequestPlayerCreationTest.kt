package player.usecases.commands

import org.junit.*
import player.adapters.storage.*
import player.domain.PlayerEvent
import player.domain.PlayerMother
import player.domain.PlayerMother.id
import player.domain.PlayerMother.name
import shared.domain.*

class RequestPlayerCreationTest {
    private val playerRepository = InMemoryPlayerRepository()
    private val eventBus = FakeEventBus()
    private val requestPlayerCreation = RequestPlayerCreation(
        playerRepository = playerRepository,
        eventBus = eventBus
    )

    @Test
    fun `should create human player when player type is human`() {
        // Given
        val playerId = id()
        val playerName = name()
        // When
        requestPlayerCreation(id = playerId, name = playerName, type = RequestPlayerCreation.PlayerType.HUMAN)
        // Then
        val storedPlayer = playerRepository.searchById(playerId)?.toDto()
        org.assertj.core.api.Assertions.assertThat(storedPlayer).isEqualTo(PlayerMother.player(playerId, playerName, type = "HUMAN").toDto())
        assertThat(eventBus).hasPublishedEvents(PlayerEvent.PlayerCreated(playerId, playerName, "HUMAN"))
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
        org.assertj.core.api.Assertions.assertThat(storedPlayer).isEqualTo(PlayerMother.player(playerId, playerName, type = "CPU").toDto())
        assertThat(eventBus).hasPublishedEvents(PlayerEvent.PlayerCreated(playerId, playerName, "CPU"))
    }

    @Test
    fun `should not create player when it already exists`() {
        // Given
        val existingPlayer = PlayerMother.player()
        playerRepository.create(existingPlayer)
        // When
        requestPlayerCreation(
            id = existingPlayer.toDto().id,
            name = existingPlayer.toDto().name,
            type = RequestPlayerCreation.PlayerType.HUMAN
        )
        // Then
        val storedPlayer = playerRepository.searchById(existingPlayer.toDto().id)?.toDto()
        org.assertj.core.api.Assertions.assertThat(storedPlayer).isEqualTo(existingPlayer.toDto())
        assertThat(eventBus).hasPublishedEvents()
    }
}
