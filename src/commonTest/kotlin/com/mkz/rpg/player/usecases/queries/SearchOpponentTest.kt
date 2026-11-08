package com.mkz.rpg.player.usecases.queries

import com.mkz.rpg.player.adapters.storage.InMemoryPlayerRepository
import com.mkz.rpg.player.domain.PlayerMother.player
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class SearchOpponentTest {
    private val playerRepository = InMemoryPlayerRepository()
    private val searchOpponent = SearchOpponent(playerRepository)

    @Test
    fun `should return the opponent when another player exists`() {
        // Given
        val player = player(id = "player-1")
        val opponent = player(id = "player-2")
        playerRepository.create(player)
        playerRepository.create(opponent)
        // When
        val storedOpponent = searchOpponent(player.toDto().id)
        // Then
        storedOpponent shouldBe opponent.toDto()
    }
}
