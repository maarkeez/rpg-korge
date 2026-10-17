package com.mkz.rpg.exploration.usecases.queries

import com.mkz.rpg.exploration.adapters.storage.InMemoryExplorationRepository
import com.mkz.rpg.exploration.domain.ExplorationMother.exploration
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class SearchExplorationTest {
    private val explorationRepository = InMemoryExplorationRepository()
    private val searchExploration = SearchExploration(explorationRepository)

    @Test
    fun `should return the exploration when the party is exploring`() {
        // Given
        val exploration = exploration()
        explorationRepository.save(exploration)
        // When
        val result = searchExploration()
        // Then
        result shouldBe exploration.toDto()
    }

    @Test
    fun `should return null when nobody explores`() {
        // When
        val result = searchExploration()
        // Then
        result.shouldBeNull()
    }
}
