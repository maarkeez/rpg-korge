package com.mkz.rpg.exploration.adapters.events

import com.mkz.rpg.battleUnit.domain.BattleUnitEvent
import com.mkz.rpg.exploration.adapters.storage.InMemoryExplorationRepository
import com.mkz.rpg.exploration.domain.ExplorationMother.exploration
import com.mkz.rpg.exploration.usecases.commands.LeaveParty
import com.mkz.rpg.shared.adapters.events.InMemoryEventBus
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class OnBattleUnitDefeatedTest {
    private val eventBus = InMemoryEventBus()
    private val explorationRepository = InMemoryExplorationRepository()
    private val onBattleUnitDefeated = OnBattleUnitDefeated(LeaveParty(explorationRepository), eventBus)

    @Test
    fun `should remove the battle unit from the party when it is defeated`() {
        // Given
        explorationRepository.save(exploration(battleUnitIds = listOf("knight-1", "knight-2")))
        eventBus.publish(BattleUnitEvent.BattleUnitDefeated(playerId = "human", battleUnitId = "knight-1", defeatedAtRow = 0, defeatedAtColumn = 0))
        // When
        eventBus.dispatch()
        // Then
        explorationRepository.search()!!.toDto().battleUnitIds shouldBe listOf("knight-2")
    }
}
