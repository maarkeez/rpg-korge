package player.usecases.commands

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import player.adapters.storage.InMemoryPlayerRepository
import player.domain.Player
import player.domain.PlayerEvent
import player.domain.PlayerMother
import player.domain.PlayerMother.id
import player.domain.PlayerMother.name
import shared.domain.FakeEventBus
import shared.domain.assertThat

class RequestPlayerCreationTest {
    private val playerRepository = InMemoryPlayerRepository()
    private val eventBus = _root_ide_package_.shared.domain.FakeEventBus()
    private val requestPlayerCreation =
        RequestPlayerCreation(
            playerRepository = playerRepository,
            eventBus = eventBus,
        )

    @Test
    fun `should create human player when player type is human`() {
        // Given
        val playerId =
            _root_ide_package_.player.domain.PlayerMother
                .id()
        val playerName =
            _root_ide_package_.player.domain.PlayerMother
                .name()
        // When
        requestPlayerCreation(id = playerId, name = playerName, type = RequestPlayerCreation.PlayerType.HUMAN)
        // Then
        val storedPlayer = playerRepository.searchById(playerId)?.toDto()
        assertThat(storedPlayer).isEqualTo(
            _root_ide_package_.player.domain.PlayerMother
                .player(playerId, playerName, type = Player.Dto.PlayerTypeDto.HUMAN)
                .toDto(),
        )
        _root_ide_package_.shared.domain
            .assertThat(eventBus)
            .hasPublishedEvents(PlayerEvent.PlayerCreated(playerId, playerName, Player.Dto.PlayerTypeDto.HUMAN))
    }

    @Test
    fun `should create cpu player when player type is cpu`() {
        // Given
        val playerId =
            _root_ide_package_.player.domain.PlayerMother
                .id()
        val playerName =
            _root_ide_package_.player.domain.PlayerMother
                .name()
        // When
        requestPlayerCreation(id = playerId, name = playerName, type = RequestPlayerCreation.PlayerType.CPU)
        // Then
        val storedPlayer = playerRepository.searchById(playerId)?.toDto()
        assertThat(storedPlayer).isEqualTo(
            _root_ide_package_.player.domain.PlayerMother
                .player(playerId, playerName, type = Player.Dto.PlayerTypeDto.CPU)
                .toDto(),
        )
        _root_ide_package_.shared.domain
            .assertThat(eventBus)
            .hasPublishedEvents(PlayerEvent.PlayerCreated(playerId, playerName, Player.Dto.PlayerTypeDto.CPU))
    }

    @Test
    fun `should not create player when it already exists`() {
        // Given
        val existingPlayer =
            _root_ide_package_.player.domain.PlayerMother
                .player()
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
        _root_ide_package_.shared.domain
            .assertThat(eventBus)
            .hasPublishedEvents()
    }
}
