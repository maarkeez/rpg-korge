package com.mkz.rpg.wandering.usecases.commands

import com.mkz.rpg.wandering.adapters.storage.InMemoryWanderingRepository
import com.mkz.rpg.wandering.domain.Wandering
import com.mkz.rpg.wandering.domain.WanderingMother.wandering
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.nulls.shouldBeNull
import kotlin.test.Test

class AwaitRespawnTest {
    private val wanderingRepository = InMemoryWanderingRepository()
    private val awaitRespawn = AwaitRespawn(wanderingRepository)

    @Test
    fun `should start counting the party steps of the defeated cpu battle unit`() {
        // Given
        wanderingRepository.save(wandering(homes = mapOf("rat-1" to (0 to 0))))
        // When
        awaitRespawn(battleUnitId = "rat-1")
        // Then
        wanderingRepository
            .search()!!
            .toDto()
            .respawns
            .shouldContainExactly(Wandering.Dto.RespawnDto(battleUnitId = "rat-1", partySteps = 0))
    }

    @Test
    fun `should do nothing when no cpu battle unit has a home`() {
        // Given
        // When
        awaitRespawn(battleUnitId = "rat-1")
        // Then
        wanderingRepository.search().shouldBeNull()
    }
}
