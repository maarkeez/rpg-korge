package com.mkz.rpg.player.usecases.queries

import com.mkz.rpg.player.adapters.storage.InMemoryPlayerRepository
import com.mkz.rpg.player.domain.PlayerMother.player
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class SearchEnemyPlayerTest {
    private val playerRepository = InMemoryPlayerRepository()
    private val searchEnemyPlayer = SearchEnemyPlayer(playerRepository)

    @Test
    fun `should return enemy player when another player exists`() {
        // Given
        val player = player(id = "player-1")
        val enemyPlayer = player(id = "player-2")
        playerRepository.create(player)
        playerRepository.create(enemyPlayer)
        // When
        val storedEnemyPlayer = searchEnemyPlayer(player.toDto().id)
        // Then
        assertThat(storedEnemyPlayer).isEqualTo(enemyPlayer.toDto())
    }
}
