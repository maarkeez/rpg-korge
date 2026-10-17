package com.mkz.rpg.wandering.adapters.storage

import com.mkz.rpg.wandering.domain.WanderingMother.wandering
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class InMemoryWanderingRepositoryTest {
    private val wanderingRepository = InMemoryWanderingRepository()

    @Test
    fun `should find nothing when no wandering has been saved`() {
        // When
        val wandering = wanderingRepository.search()
        // Then
        wandering.shouldBeNull()
    }

    @Test
    fun `should find the last saved wandering`() {
        // Given
        wanderingRepository.save(wandering(homes = mapOf("rat-1" to (0 to 0))))
        wanderingRepository.save(wandering(homes = mapOf("rat-2" to (1 to 1))))
        // When
        val wandering = wanderingRepository.search()
        // Then
        wandering shouldBe wandering(homes = mapOf("rat-2" to (1 to 1)))
    }
}
