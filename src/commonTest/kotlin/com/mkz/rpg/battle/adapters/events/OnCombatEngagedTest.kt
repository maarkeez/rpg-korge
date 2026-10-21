package com.mkz.rpg.battle.adapters.events

import com.mkz.rpg.battle.adapters.storage.InMemoryBattleRepository
import com.mkz.rpg.battle.usecases.commands.StartBattle
import com.mkz.rpg.cpuBattleBrain.domain.CpuBattleBrainEvent
import com.mkz.rpg.shared.adapters.events.InMemoryEventBus
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class OnCombatEngagedTest {
    private val eventBus = InMemoryEventBus()
    private val battleRepository = InMemoryBattleRepository()
    private val onCombatEngaged = OnCombatEngaged(StartBattle(battleRepository, eventBus), eventBus)

    @Test
    fun `should start the battle with the engaging cpu battle units and the party when the cpu engages`() {
        // Given
        eventBus.publish(
            CpuBattleBrainEvent.CombatEngaged(playerId = "cpu", engagedPlayerId = "human", battleUnitIds = listOf("rat-1"), engagedBattleUnitIds = listOf("knight-1")),
        )
        // When
        eventBus.dispatch()
        // Then
        val battle = battleRepository.search()!!.toDto()
        battle.currentPlayerTurn shouldBe "cpu"
        battle.battleUnitIds shouldBe setOf("rat-1", "knight-1")
    }
}
