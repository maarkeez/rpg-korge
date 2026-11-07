package com.mkz.rpg.worldBuilder.usecases.queries

import com.mkz.rpg.worldBuilder.adapters.storage.InMemoryWorldBuilderRepository
import com.mkz.rpg.worldBuilder.domain.WorldBuilder
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class SearchWorldTest {
    private val worldBuilderRepository = InMemoryWorldBuilderRepository()
    private val searchWorld = SearchWorld(worldBuilderRepository)

    @Test
    fun `should return the world when it has been created`() {
        // Given
        val worldBuilder = WorldBuilder.create()
        worldBuilderRepository.save(worldBuilder)
        // When
        val world = searchWorld()
        // Then
        world shouldBe worldBuilder.toDto()
    }

    @Test
    fun `should return null when the world has not been created`() {
        // When
        val world = searchWorld()
        // Then
        world.shouldBeNull()
    }
}
