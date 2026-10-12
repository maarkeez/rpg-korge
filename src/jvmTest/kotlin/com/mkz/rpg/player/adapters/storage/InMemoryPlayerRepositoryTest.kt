package com.mkz.rpg.player.adapters.storage

import com.mkz.rpg.player.domain.PlayerMother
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class InMemoryPlayerRepositoryTest {
    private val playerRepository = InMemoryPlayerRepository()

    @Test
    fun `Create should store the player when the player is created`() {
        // Given
        val player = PlayerMother.player()
        // When
        playerRepository.create(player)
        // Then
        val storedPlayer = playerRepository.searchById(player.toDto().id)
        storedPlayer shouldBe player
    }

    @Test
    fun `SearchById should return the player when the player is stored`() {
        // Given
        val player = PlayerMother.player()
        playerRepository.create(player)
        // When
        val storedPlayer = playerRepository.searchById(player.toDto().id)
        // Then
        storedPlayer shouldBe player
    }

    @Test
    fun `SearchById should return null when the player is not stored`() {
        // When
        val storedPlayer = playerRepository.searchById("player-unknown")
        // Then
        storedPlayer.shouldBeNull()
    }

    @Test
    fun `SearchEnemy should return the other player when there are two players stored`() {
        // Given
        val player = PlayerMother.player(id = "player-1")
        val enemyPlayer = PlayerMother.player(id = "player-2")
        playerRepository.create(player)
        playerRepository.create(enemyPlayer)
        // When
        val storedEnemyPlayer = playerRepository.searchEnemy(playerId = player.toDto().id)
        // Then
        storedEnemyPlayer shouldBe enemyPlayer
    }
}
