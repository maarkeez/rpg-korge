package player.usecases.queries

import org.assertj.core.api.Assertions.assertThat
import org.junit.*
import player.adapters.storage.*
import player.domain.PlayerMother.player

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
