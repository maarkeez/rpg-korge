package com.mkz.rpg.exploration.usecases.commands

import com.mkz.rpg.exploration.adapters.storage.InMemoryExplorationRepository
import com.mkz.rpg.exploration.domain.ExplorationMother.exploration
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class MarkBattleUnitMovedTest {
    private val explorationRepository = InMemoryExplorationRepository()
    private val markBattleUnitMoved = MarkBattleUnitMoved(explorationRepository)

    @Test
    fun `should remember the battle unit moved during the current second when it moves while exploring`() {
        // Given
        explorationRepository.save(exploration())
        // When
        markBattleUnitMoved("knight-1")
        // Then
        explorationRepository.search()!!.standingBattleUnitIds(setOf("knight-1", "knight-2")) shouldBe setOf("knight-2")
    }

    @Test
    fun `should do nothing when nobody explores`() {
        // When
        markBattleUnitMoved("knight-1")
        // Then
        explorationRepository.search().shouldBeNull()
    }
}
