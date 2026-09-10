package com.mkz.rpg.player.usecases.queries

import com.mkz.rpg.player.adapters.storage.InMemoryPlayerRepository
import com.mkz.rpg.player.domain.PlayerMother.id
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class SearchPlayerByIdTest {
    private val playerRepository = InMemoryPlayerRepository()
    private val searchPlayerById = SearchPlayerById(playerRepository)

    @Test
    fun `should return player when it exists`() {
        // Given
        val player =
            com.mkz.rpg.player.domain.PlayerMother
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
