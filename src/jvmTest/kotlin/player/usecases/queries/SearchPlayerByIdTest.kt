package player.usecases.queries

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import player.adapters.storage.InMemoryPlayerRepository
import player.domain.PlayerMother.id

class SearchPlayerByIdTest {
    private val playerRepository = InMemoryPlayerRepository()
    private val searchPlayerById = SearchPlayerById(playerRepository)

    @Test
    fun `should return player when it exists`() {
        // Given
        val player =
            player.domain.PlayerMother
                .player()
        val playerDto = player.toDto()
        playerRepository.create(player)
        // When
        val storedPlayer = searchPlayerById(playerDto.id)
        // Then
        assertThat(storedPlayer).isEqualTo(playerDto)
    }

    @Test
    fun `should return null when player does not exist`() {
        // Given
        val unknownPlayerId = id()
        // When
        val storedPlayer = searchPlayerById(unknownPlayerId)
        // Then
        assertThat(storedPlayer).isNull()
    }
}
