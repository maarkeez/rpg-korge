package com.mkz.rpg.player.usecases.queries

import com.mkz.rpg.player.adapters.storage.InMemoryPlayerRepository
import com.mkz.rpg.player.domain.Player.Dto.PlayerTypeDto
import com.mkz.rpg.player.domain.PlayerMother.player
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class SearchHumanPlayerTest {
    private val playerRepository = InMemoryPlayerRepository()
    private val searchHumanPlayer = SearchHumanPlayer(playerRepository)

    @Test
    fun `should return the human player when a cpu player is created first`() {
        // Given
        playerRepository.create(player(id = "cpu", type = PlayerTypeDto.CPU))
        val human = player(id = "human", type = PlayerTypeDto.HUMAN)
        playerRepository.create(human)
        // When
        val result = searchHumanPlayer()
        // Then
        result shouldBe human.toDto()
    }

    @Test
    fun `should return null when every player is a cpu`() {
        // Given
        playerRepository.create(player(id = "cpu", type = PlayerTypeDto.CPU))
        // When
        val result = searchHumanPlayer()
        // Then
        result.shouldBeNull()
    }
}
