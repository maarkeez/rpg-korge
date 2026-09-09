package battle.usecases.commands

import battle.adapters.storage.InMemoryBattleRepository
import battle.domain.BattleEvent
import battle.domain.BattleMother.battle
import battle.domain.BattleMother.finishedBattle
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import shared.domain.FakeEventBus
import shared.domain.assertThat

class FinishBattleTest {
    private val battleRepository = InMemoryBattleRepository()
    private val eventBus = FakeEventBus()
    private val finishBattle =
        FinishBattle(
            battleRepository = battleRepository,
            eventBus = eventBus,
        )

    @Test
    fun `should finish battle when the battle is finished`() {
        // Given
        val players = listOf("player-1", "player-2")
        val winner = players[1]
        battleRepository.create(finishedBattle(players))
        // When
        finishBattle()
        // Then
        assertThat(eventBus)
            .hasPublishedEvents(BattleEvent.PlayerVictory(winner))
    }

    @Test
    fun `should not finish battle when the battle is not finished`() {
        // Given
        val players = listOf("player-1", "player-2")
        battleRepository.create(battle(players))
        // When
        finishBattle()
        // Then
        assertThat(eventBus.publishedEvents).isEmpty()
    }
}
