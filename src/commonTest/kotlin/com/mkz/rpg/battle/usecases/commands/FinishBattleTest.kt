package com.mkz.rpg.battle.usecases.commands

import com.mkz.rpg.battle.adapters.storage.InMemoryBattleRepository
import com.mkz.rpg.battle.domain.BattleEvent
import com.mkz.rpg.battle.domain.BattleMother.battle
import com.mkz.rpg.battle.domain.BattleMother.finishedBattle
import com.mkz.rpg.shared.domain.FakeEventBus
import com.mkz.rpg.shared.domain.shouldHavePublishedEvents
import io.kotest.matchers.collections.shouldBeEmpty
import kotlin.test.Test

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
        eventBus.shouldHavePublishedEvents(BattleEvent.PlayerVictory(winner))
    }

    @Test
    fun `should not finish battle when the battle is not finished`() {
        // Given
        val players = listOf("player-1", "player-2")
        battleRepository.create(battle(players))
        // When
        finishBattle()
        // Then
        eventBus.publishedEvents.shouldBeEmpty()
    }
}
