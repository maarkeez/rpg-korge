package com.mkz.rpg.battle.adapters.events

import com.mkz.rpg.battle.adapters.storage.InMemoryBattleRepository
import com.mkz.rpg.battle.domain.BattleMother.battle
import com.mkz.rpg.battle.usecases.commands.StartNextRound
import com.mkz.rpg.cpuBrain.domain.CpuBrainEvent
import com.mkz.rpg.shared.adapters.events.InMemoryEventBus
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class OnEngagementUpdatedTest {
    private val eventBus = InMemoryEventBus()
    private val battleRepository = InMemoryBattleRepository()
    private val onEngagementUpdated =
        OnEngagementUpdated(
            eventBus = eventBus,
            startNextRound = StartNextRound(battleRepository, eventBus),
        )

    @Test
    fun `should start next round with the joining battle units when the engagement is updated`() {
        // Given
        battleRepository.create(battle(players = listOf("player-1", "player-2"), battleUnitIds = setOf("battle-unit-1")))
        eventBus.publish(CpuBrainEvent.EngagementUpdated(joiningBattleUnitIds = listOf("battle-unit-2")))
        // When
        eventBus.dispatch()
        // Then
        val battle = battleRepository.search()!!.toDto()
        battle.currentRound shouldBe 2
        battle.battleUnitIds shouldBe setOf("battle-unit-1", "battle-unit-2")
    }
}
