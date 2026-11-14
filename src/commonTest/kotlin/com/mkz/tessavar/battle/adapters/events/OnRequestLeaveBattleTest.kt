package com.mkz.tessavar.battle.adapters.events

import com.mkz.tessavar.battle.adapters.storage.InMemoryBattleRepository
import com.mkz.tessavar.battle.domain.BattleEvent
import com.mkz.tessavar.battle.domain.BattleMother.battle
import com.mkz.tessavar.battle.usecases.commands.LeaveBattle
import com.mkz.tessavar.shared.adapters.events.InMemoryEventBus
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class OnRequestLeaveBattleTest {
    private val eventBus = InMemoryEventBus()
    private val battleRepository = InMemoryBattleRepository()
    private val onRequestLeaveBattle = OnRequestLeaveBattle(LeaveBattle(battleRepository, eventBus), eventBus)

    @Test
    fun `should remove the battle unit from the battle when it is requested to leave it`() {
        // Given
        battleRepository.create(battle(battleUnitIds = setOf("battle-unit-1", "battle-unit-2")))
        eventBus.publish(BattleEvent.RequestLeaveBattle(battleUnitId = "battle-unit-1"))
        // When
        eventBus.dispatch()
        // Then
        battleRepository.search()?.toDto()?.battleUnitIds shouldBe setOf("battle-unit-2")
    }
}
