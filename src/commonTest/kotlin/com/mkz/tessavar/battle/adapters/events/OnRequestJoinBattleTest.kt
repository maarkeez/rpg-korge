package com.mkz.tessavar.battle.adapters.events

import com.mkz.tessavar.battle.adapters.storage.InMemoryBattleRepository
import com.mkz.tessavar.battle.domain.BattleEvent
import com.mkz.tessavar.battle.domain.BattleMother.battle
import com.mkz.tessavar.battle.usecases.commands.JoinBattle
import com.mkz.tessavar.shared.adapters.events.InMemoryEventBus
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class OnRequestJoinBattleTest {
    private val eventBus = InMemoryEventBus()
    private val battleRepository = InMemoryBattleRepository()
    private val onRequestJoinBattle = OnRequestJoinBattle(JoinBattle(battleRepository), eventBus)

    @Test
    fun `should add the battle unit to the battle when it is requested to join it`() {
        // Given
        battleRepository.create(battle(battleUnitIds = setOf("battle-unit-1")))
        eventBus.publish(BattleEvent.RequestJoinBattle(battleUnitId = "battle-unit-2", playerId = "player-1"))
        // When
        eventBus.dispatch()
        // Then
        battleRepository.search()?.toDto()?.battleUnitIds shouldBe setOf("battle-unit-1", "battle-unit-2")
    }
}
