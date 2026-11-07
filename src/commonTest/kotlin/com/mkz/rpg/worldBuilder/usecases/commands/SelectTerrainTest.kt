package com.mkz.rpg.worldBuilder.usecases.commands

import com.mkz.rpg.shared.domain.FakeEventBus
import com.mkz.rpg.shared.domain.shouldHavePublishedEvents
import com.mkz.rpg.worldBuilder.adapters.storage.InMemoryWorldBuilderRepository
import com.mkz.rpg.worldBuilder.domain.WorldBuilder
import com.mkz.rpg.worldBuilder.domain.WorldBuilderError.WorldNotCreated
import com.mkz.rpg.worldBuilder.domain.WorldBuilderEvent.TerrainSelected
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class SelectTerrainTest {
    private val worldBuilderRepository = InMemoryWorldBuilderRepository()
    private val eventBus = FakeEventBus()
    private val selectTerrain = SelectTerrain(worldBuilderRepository, eventBus)

    @Test
    fun `should store the selected terrain and announce it when a terrain is selected`() {
        // Given
        worldBuilderRepository.save(WorldBuilder.create().pullEvents().second)
        // When
        selectTerrain("lava")
        // Then
        worldBuilderRepository.search()?.toDto()?.selectedTerrainId shouldBe "lava"
        eventBus.shouldHavePublishedEvents(TerrainSelected(terrainId = "lava"))
    }

    @Test
    fun `should fail when the world has not been created`() {
        // When
        val select = { selectTerrain("lava") }
        // Then
        shouldThrow<WorldNotCreated> { select() }
    }
}
