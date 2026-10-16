package com.mkz.rpg.mapBuilder.usecases.commands

import com.mkz.rpg.mapBuilder.adapters.storage.InMemoryMapBuilderRepository
import com.mkz.rpg.mapBuilder.domain.MapBuilder
import com.mkz.rpg.mapBuilder.domain.MapBuilderError.MapNotCreated
import com.mkz.rpg.mapBuilder.domain.MapBuilderEvent.TerrainSelected
import com.mkz.rpg.shared.domain.FakeEventBus
import com.mkz.rpg.shared.domain.shouldHavePublishedEvents
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class SelectTerrainTest {
    private val mapBuilderRepository = InMemoryMapBuilderRepository()
    private val eventBus = FakeEventBus()
    private val selectTerrain = SelectTerrain(mapBuilderRepository, eventBus)

    @Test
    fun `should store the selected terrain and announce it when a terrain is selected`() {
        // Given
        mapBuilderRepository.save(MapBuilder.create().pullEvents().second)
        // When
        selectTerrain("lava")
        // Then
        mapBuilderRepository.search()?.toDto()?.selectedTerrainId shouldBe "lava"
        eventBus.shouldHavePublishedEvents(TerrainSelected(terrainId = "lava"))
    }

    @Test
    fun `should fail when the map has not been created`() {
        // When
        val select = { selectTerrain("lava") }
        // Then
        shouldThrow<MapNotCreated> { select() }
    }
}
