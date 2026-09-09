package player.adapters.storage

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import player.domain.PlayerMother

class InMemoryPlayerRepositoryTest {
    private val playerRepository = InMemoryPlayerRepository()

    @Nested
    inner class Create {
        @Test
        fun `should store the player when the player is created`() {
            // Given
            val player = PlayerMother.player()
            // When
            playerRepository.create(player)
            // Then
            val storedPlayer = playerRepository.searchById(player.toDto().id)
            assertThat(storedPlayer).isEqualTo(player)
        }
    }

    @Nested
    inner class SearchById {
        @Test
        fun `should return the player when the player is stored`() {
            // Given
            val player = PlayerMother.player()
            playerRepository.create(player)
            // When
            val storedPlayer = playerRepository.searchById(player.toDto().id)
            // Then
            assertThat(storedPlayer).isEqualTo(player)
        }

        @Test
        fun `should return null when the player is not stored`() {
            // When
            val storedPlayer = playerRepository.searchById("player-unknown")
            // Then
            assertThat(storedPlayer).isNull()
        }
    }

    @Nested
    inner class SearchEnemy {
        @Test
        fun `should return the other player when there are two players stored`() {
            // Given
            val player = PlayerMother.player(id = "player-1")
            val enemyPlayer = PlayerMother.player(id = "player-2")
            playerRepository.create(player)
            playerRepository.create(enemyPlayer)
            // When
            val storedEnemyPlayer = playerRepository.searchEnemy(playerId = player.toDto().id)
            // Then
            assertThat(storedEnemyPlayer).isEqualTo(enemyPlayer)
        }
    }
}
