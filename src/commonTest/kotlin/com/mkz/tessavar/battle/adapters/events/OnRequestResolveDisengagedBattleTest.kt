package com.mkz.tessavar.battle.adapters.events

import com.mkz.tessavar.battle.adapters.storage.InMemoryBattleRepository
import com.mkz.tessavar.battle.domain.BattleEvent
import com.mkz.tessavar.battle.domain.BattleMother.battle
import com.mkz.tessavar.battle.usecases.commands.ResolveDisengagedBattle
import com.mkz.tessavar.shared.adapters.events.InMemoryEventBus
import io.kotest.matchers.booleans.shouldBeTrue
import kotlin.test.Test

class OnRequestResolveDisengagedBattleTest {
    private val eventBus = InMemoryEventBus()
    private val battleRepository = InMemoryBattleRepository()
    private val onRequestResolveDisengagedBattle = OnRequestResolveDisengagedBattle(ResolveDisengagedBattle(battleRepository, eventBus), eventBus)

    @Test
    fun `should resolve the disengaged battle when it is requested`() {
        // Given
        battleRepository.create(battle(players = listOf("cpu", "human"), battleUnits = mapOf("rat-1" to "cpu", "knight-1" to "human")).leave("rat-1"))
        eventBus.publish(BattleEvent.RequestResolveDisengagedBattle(joiningPlayerId = "cpu", joiningBattleUnitIds = emptyList()))
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
