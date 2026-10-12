package com.mkz.rpg.battle.usecases.commands

import com.mkz.rpg.battle.adapters.storage.InMemoryBattleRepository
import com.mkz.rpg.battle.domain.Battle
import com.mkz.rpg.battle.domain.BattleEvent
import com.mkz.rpg.battle.domain.BattleMother.battle
import com.mkz.rpg.shared.domain.FakeEventBus
import com.mkz.rpg.shared.domain.shouldHavePublishedEvents
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import kotlin.test.Test

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
        storedBattle shouldBe Battle.Dto(currentPlayerTurn = players.first(), currentRound = 2, isFinished = false)
        eventBus.shouldHavePublishedEvents(
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
        battleRepository.search().shouldBeNull()
        eventBus.publishedEvents.shouldBeEmpty()
    }
}
