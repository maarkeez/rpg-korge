package com.mkz.rpg.battle.usecases.commands

import com.mkz.rpg.battle.adapters.storage.InMemoryBattleRepository
import com.mkz.rpg.battle.domain.BattleEvent
import com.mkz.rpg.battle.domain.BattleMother.battle
import com.mkz.rpg.battle.domain.BattleMother.finishedBattle
import com.mkz.rpg.shared.domain.FakeEventBus
import com.mkz.rpg.shared.domain.shouldHavePublishedEvents
import io.kotest.matchers.booleans.shouldBeTrue
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
    fun `should finish the battle in progress and publish that it finished`() {
        // Given
        battleRepository.create(battle())
        // When
        finishBattle()
        // Then
        battleRepository
            .search()!!
            .toDto()
            .isFinished
            .shouldBeTrue()
        eventBus.shouldHavePublishedEvents(BattleEvent.BattleFinished)
    }

    @Test
    fun `should do nothing when the battle is already finished`() {
        // Given
        battleRepository.create(finishedBattle())
        // When
        finishBattle()
        // Then
        eventBus.publishedEvents.shouldBeEmpty()
    }

    @Test
    fun `should do nothing when there is no battle`() {
        // When
        finishBattle()
        // Then
        eventBus.publishedEvents.shouldBeEmpty()
    }
}
