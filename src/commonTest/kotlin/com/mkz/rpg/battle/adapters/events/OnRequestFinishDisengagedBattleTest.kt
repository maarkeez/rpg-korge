package com.mkz.rpg.battle.adapters.events

import com.mkz.rpg.battle.adapters.storage.InMemoryBattleRepository
import com.mkz.rpg.battle.domain.BattleEvent
import com.mkz.rpg.battle.domain.BattleMother.battle
import com.mkz.rpg.battle.usecases.commands.FinishDisengagedBattle
import com.mkz.rpg.shared.adapters.events.InMemoryEventBus
import io.kotest.matchers.booleans.shouldBeTrue
import kotlin.test.Test

class OnRequestFinishDisengagedBattleTest {
    private val eventBus = InMemoryEventBus()
    private val battleRepository = InMemoryBattleRepository()
    private val onRequestFinishDisengagedBattle = OnRequestFinishDisengagedBattle(FinishDisengagedBattle(battleRepository, eventBus), eventBus)

    @Test
    fun `should finish the disengaged battle when it is requested`() {
        // Given
        battleRepository.create(battle(players = listOf("cpu", "human"), battleUnits = mapOf("rat-1" to "cpu", "knight-1" to "human")).leave("rat-1"))
        eventBus.publish(BattleEvent.RequestFinishDisengagedBattle(joiningPlayerId = "cpu", joiningBattleUnitIds = emptyList()))
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
