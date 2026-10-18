package com.mkz.rpg.wandering.usecases.commands

import com.mkz.rpg.wandering.adapters.storage.InMemoryWanderingRepository
import com.mkz.rpg.wandering.domain.WanderingMother.wandering
import io.kotest.matchers.collections.shouldBeEmpty
import kotlin.test.Test

class ResolveRespawnTest {
    private val wanderingRepository = InMemoryWanderingRepository()
    private val resolveRespawn = ResolveRespawn(wanderingRepository)

    @Test
    fun `should stop counting the party steps of the respawned battle unit`() {
        // Given
        wanderingRepository.save(wandering(homes = mapOf("rat-1" to (0 to 0))).defeat("rat-1").walkPartyStep())
        // When
        resolveRespawn(battleUnitId = "rat-1")
        // Then
        wanderingRepository
            .search()!!
            .toDto()
            .respawns
            .shouldBeEmpty()
    }
}
