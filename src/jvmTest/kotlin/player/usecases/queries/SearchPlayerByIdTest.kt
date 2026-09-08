package player.usecases.queries

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import player.adapters.storage.*
import player.domain.*
import player.domain.PlayerMother.player

class SearchPlayerByIdTest {
    private val playerRepository = InMemoryPlayerRepository()
    private val searchPlayerById = SearchPlayerById(playerRepository)

    @Test
    fun `should return player when it exists`() {
        // Given
        val player = player()
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
        val unknownPlayerId = PlayerMother.id()
        // When
        val storedPlayer = searchPlayerById(unknownPlayerId)
        // Then
        assertThat(storedPlayer).isNull()
    }
}
