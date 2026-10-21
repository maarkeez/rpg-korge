package com.mkz.rpg.battle.adapters.events

import com.mkz.rpg.battle.adapters.storage.InMemoryBattleRepository
import com.mkz.rpg.battle.domain.BattleEvent
import com.mkz.rpg.battle.domain.BattleMother.battle
import com.mkz.rpg.battle.usecases.commands.StartNextRound
import com.mkz.rpg.shared.adapters.events.InMemoryEventBus
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class OnRequestStartNextRoundTest {
    private val eventBus = InMemoryEventBus()
    private val battleRepository = InMemoryBattleRepository()
    private val onRequestStartNextRound =
        OnRequestStartNextRound(
            eventBus = eventBus,
            startNextRound = StartNextRound(battleRepository, eventBus),
        )

    @Test
    fun `should start next round with the joining battle units and without the leaving ones when it is requested`() {
        // Given
        battleRepository.create(battle(players = listOf("cpu", "human"), battleUnits = mapOf("rat-1" to "cpu", "knight-1" to "human")))
        eventBus.publish(BattleEvent.RequestStartNextRound(joiningPlayerId = "cpu", joiningBattleUnitIds = listOf("rat-2"), leavingBattleUnitIds = listOf("rat-1")))
        // When
        eventBus.dispatch()
        // Then
        val battle = battleRepository.search()!!.toDto()
        battle.currentRound shouldBe 2
        battle.battleUnitIds shouldBe setOf("rat-2", "knight-1")
    }
}
