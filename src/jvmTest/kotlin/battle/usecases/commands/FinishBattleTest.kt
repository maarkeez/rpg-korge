package battle.usecases.commands

import battle.adapters.storage.*
import battle.domain.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.*
import shared.domain.*
import shared.domain.assertThat

class FinishBattleTest {
    private val battleRepository = InMemoryBattleRepository()
    private val eventBus = FakeEventBus()
    private val finishBattle = FinishBattle(
        battleRepository = battleRepository,
        eventBus = eventBus,
    )

    @Test
    fun `should finish battle when the battle is finished`() {
        // Given
        val players = listOf("player-1", "player-2")
        val winner = players[1]
        battleRepository.create(BattleMother.finishedBattle(players))
        // When
        finishBattle()
        // Then
        assertThat(eventBus).hasPublishedEvents(BattleEvent.PlayerVictory(winner))
    }

    @Test
    fun `should not finish battle when the battle is not finished`() {
        // Given
        val players = listOf("player-1", "player-2")
        battleRepository.create(BattleMother.battle(players))
        // When
        finishBattle()
        // Then
        assertThat(eventBus.publishedEvents).isEmpty()
    }
}
