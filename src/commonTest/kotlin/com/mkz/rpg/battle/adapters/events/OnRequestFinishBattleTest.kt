package com.mkz.rpg.battle.adapters.events

import com.mkz.rpg.battle.adapters.storage.InMemoryBattleRepository
import com.mkz.rpg.battle.domain.BattleEvent
import com.mkz.rpg.battle.domain.BattleMother.battle
import com.mkz.rpg.battle.usecases.commands.FinishBattle
import com.mkz.rpg.shared.adapters.events.InMemoryEventBus
import io.kotest.matchers.booleans.shouldBeTrue
import kotlin.test.Test

class OnRequestFinishBattleTest {
    private val eventBus = InMemoryEventBus()
    private val battleRepository = InMemoryBattleRepository()
    private val onRequestFinishBattle = OnRequestFinishBattle(FinishBattle(battleRepository, eventBus), eventBus)

    @Test
    fun `should finish the battle when it is requested to finish`() {
        // Given
        battleRepository.create(battle())
        eventBus.publish(BattleEvent.RequestFinishBattle)
        // When
        eventBus.dispatch()
        // Then
        battleRepository
            .search()!!
            .toDto()
            .isFinished
            .shouldBeTrue()
    }
}
