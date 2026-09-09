package battle.usecases.commands

import battle.adapters.storage.InMemoryBattleRepository
import battle.domain.Battle
import battle.domain.BattleEvent
import battle.domain.BattleMother
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import shared.domain.FakeEventBus
import shared.domain.assertThat

class FinishPlayerTurnTest {
    private val battleRepository = InMemoryBattleRepository()
    private val eventBus = FakeEventBus()
    private val finishPlayerTurn =
        FinishPlayerTurn(
            battleRepository = battleRepository,
            eventBus = eventBus,
        )

    @Test
    fun `should start next player turn when another player remains in the queue`() {
        // Given
        val players = listOf("player-1", "player-2")
        battleRepository.create(BattleMother.battle(players))
        // When
        finishPlayerTurn()
        // Then
        val storedBattle = battleRepository.search()?.toDto()
        assertThat(storedBattle)
            .isEqualTo(Battle.Dto(currentPlayerTurn = players[1], currentRound = 1))
        assertThat(eventBus).hasPublishedEvents(BattleEvent.PlayerTurnStarted(players[1]))
    }

    @Test
    fun `should finish round when the current player is the last one in the queue`() {
        // Given
        val players = listOf("player-1", "player-2")
        val battle =
            BattleMother
                .battle(players)
                .finishPlayerTurn()
                .pullEvents()
                .second
        battleRepository.create(battle)
        // When
        finishPlayerTurn()
        // Then
        val storedBattle = battleRepository.search()?.toDto()
        assertThat(storedBattle)
            .isEqualTo(Battle.Dto(currentPlayerTurn = players[1], currentRound = 1))
        assertThat(eventBus).hasPublishedEvents(BattleEvent.BattleRoundFinished(1))
    }
}
