package com.mkz.rpg.mapBuilder.usecases.commands

import com.mkz.rpg.mapBuilder.adapters.storage.InMemoryMapBuilderRepository
import com.mkz.rpg.mapBuilder.domain.MapBuilder
import com.mkz.rpg.mapBuilder.domain.MapBuilderEvent.MapCreated
import com.mkz.rpg.shared.domain.FakeEventBus
import com.mkz.rpg.shared.domain.shouldHavePublishedEvents
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class CreateMapTest {
    private val mapBuilderRepository = InMemoryMapBuilderRepository()
    private val eventBus = FakeEventBus()
    private val createMap = CreateMap(mapBuilderRepository, eventBus)

    @Test
    fun `should store a new map and announce it when a map is created`() {
        // When
        createMap()
        // Then
        mapBuilderRepository.search()?.toDto() shouldBe MapBuilder.create().toDto()
        eventBus.shouldHavePublishedEvents(MapCreated(rows = 16, columns = 16))
    }

    @Test
    fun `should replace the previous map when a map is created again`() {
        // Given
        createMap()
        ResizeMap(mapBuilderRepository, FakeEventBus())(rows = 4, columns = 4)
        // When
        createMap()
        // Then
        mapBuilderRepository.search()?.toDto()?.rows shouldBe 16
    }
}
