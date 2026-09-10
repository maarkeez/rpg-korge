package com.mkz.rpg.battle.usecases.commands

import com.mkz.rpg.battle.adapters.storage.InMemoryBattleRepository
import com.mkz.rpg.battle.domain.Battle
import com.mkz.rpg.battle.domain.BattleError
import com.mkz.rpg.battle.domain.BattleEvent
import com.mkz.rpg.shared.domain.FakeEventBus
import com.mkz.rpg.shared.domain.assertThat
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class StartFirstRoundTest {
    private val battleRepository = InMemoryBattleRepository()
    private val eventBus = FakeEventBus()
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
        assertThat(eventBus).hasPublishedEvents(
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
