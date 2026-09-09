package battle.usecases.commands

import battle.adapters.storage.InMemoryBattleRepository
import battle.domain.Battle
import battle.domain.BattleError
import battle.domain.BattleEvent
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import shared.domain.FakeEventBus
import shared.domain.assertThat

class StartFirstRoundTest {
    private val battleRepository = InMemoryBattleRepository()
    private val eventBus = _root_ide_package_.shared.domain.FakeEventBus()
    private val startFirstRound =
        StartFirstRound(
            battleRepository = battleRepository,
            eventBus = eventBus,
        )

    @Test
    fun `should create battle when at least two players are provided`() {
        // Given
        val players = listOf("player-1", "player-2")
        // When
        startFirstRound(players = players)
        // Then
        val storedBattle = battleRepository.search()?.toDto()
        assertThat(storedBattle)
            .isEqualTo(Battle.Dto(currentPlayerTurn = players.first(), currentRound = 1))
        _root_ide_package_.shared.domain.assertThat(eventBus).hasPublishedEvents(
            BattleEvent.BattleStarted,
            BattleEvent.PlayerTurnStarted(players.first()),
        )
    }

    @Test
    fun `should throw battle error when fewer than two players are provided`() {
        // Given
        val players = listOf("player-1")
        // When
        val error =
            org.assertj.core.api.Assertions.catchThrowable {
                startFirstRound(players = players)
            }
        // Then
        assertThat(error)
            .isInstanceOf(BattleError.MinimumTwoPlayersRequired::class.java)
    }
}
