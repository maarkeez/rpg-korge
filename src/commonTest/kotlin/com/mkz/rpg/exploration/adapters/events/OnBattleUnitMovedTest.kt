package com.mkz.rpg.exploration.adapters.events

import com.mkz.rpg.battleUnit.domain.BattleUnitEvent
import com.mkz.rpg.exploration.adapters.storage.InMemoryExplorationRepository
import com.mkz.rpg.exploration.domain.ExplorationMother.exploration
import com.mkz.rpg.exploration.usecases.commands.MarkBattleUnitMoved
import com.mkz.rpg.shared.adapters.events.InMemoryEventBus
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class OnBattleUnitMovedTest {
    private val eventBus = InMemoryEventBus()
    private val explorationRepository = InMemoryExplorationRepository()
    private val onBattleUnitMoved = OnBattleUnitMoved(MarkBattleUnitMoved(explorationRepository), eventBus)

    @Test
    fun `should remember the battle unit moved during the current second when it moves`() {
        // Given
        explorationRepository.save(exploration())
        eventBus.publish(BattleUnitEvent.BattleUnitMoved(battleUnitId = "knight-1", fromRow = 0, fromColumn = 0, toRow = 0, toColumn = 1, path = emptyList()))
        // When
        eventBus.dispatch()
        // Then
        explorationRepository.search()!!.standingBattleUnitIds(setOf("knight-1", "knight-2")) shouldBe setOf("knight-2")
    }
}
