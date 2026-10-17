package com.mkz.rpg.exploration.usecases.commands

import com.mkz.rpg.exploration.adapters.storage.InMemoryExplorationRepository
import com.mkz.rpg.exploration.domain.ExplorationMother.exploration
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class LeavePartyTest {
    private val explorationRepository = InMemoryExplorationRepository()
    private val leaveParty = LeaveParty(explorationRepository)

    @Test
    fun `should have the next battle unit lead when the leader leaves the party`() {
        // Given
        explorationRepository.save(exploration(battleUnitIds = listOf("knight-1", "knight-2")))
        // When
        leaveParty("knight-1")
        // Then
        val exploration = explorationRepository.search()!!.toDto()
        exploration.leaderBattleUnitId shouldBe "knight-2"
        exploration.battleUnitIds shouldBe listOf("knight-2")
    }

    @Test
    fun `should keep the party when the battle unit is not in it`() {
        // Given
        val storedExploration = exploration(battleUnitIds = listOf("knight-1", "knight-2"))
        explorationRepository.save(storedExploration)
        // When
        leaveParty("rat-1")
        // Then
        explorationRepository.search() shouldBe storedExploration
    }

    @Test
    fun `should do nothing when nobody explores`() {
        // When
        leaveParty("knight-1")
        // Then
        explorationRepository.search().shouldBeNull()
    }
}
