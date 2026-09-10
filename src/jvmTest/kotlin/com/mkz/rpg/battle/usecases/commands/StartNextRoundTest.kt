package com.mkz.rpg.battle.usecases.commands

import com.mkz.rpg.battle.adapters.storage.InMemoryBattleRepository
import com.mkz.rpg.battle.domain.Battle
import com.mkz.rpg.battle.domain.BattleEvent
import com.mkz.rpg.battle.domain.BattleMother.battle
import com.mkz.rpg.shared.domain.FakeEventBus
import com.mkz.rpg.shared.domain.assertThat
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class StartNextRoundTest {
    private val battleRepository = InMemoryBattleRepository()
    private val eventBus = FakeEventBus()
    private val startNextRound =
        StartNextRound(
            battleRepository = battleRepository,
            eventBus = eventBus,
        )

    @Test
    fun `should start next round when a battle exists`() {
        // Given
        val players = listOf("player-1", "player-2")
        battleRepository.create(battle(players))
        // When
        startNextRound()
        // Then
        val storedBattle = battleRepository.search()?.toDto()
        assertThat(storedBattle)
            .isEqualTo(Battle.Dto(currentPlayerTurn = players.first(), currentRound = 2))
        assertThat(eventBus).hasPublishedEvents(
            BattleEvent.BattleRoundStarted(2),
            BattleEvent.PlayerTurnStarted(players.first()),
        )
    }

    @Test
    fun `should not start next round when no battle exists`() {
        // Given
        // When
        startNextRound()
        // Then
        assertThat(battleRepository.search()).isNull()
        assertThat(eventBus.publishedEvents).isEmpty()
    }
}
