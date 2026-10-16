package com.mkz.rpg.mapBuilder.usecases.commands

import com.mkz.rpg.mapBuilder.adapters.storage.InMemoryMapBuilderRepository
import com.mkz.rpg.mapBuilder.domain.MapBuilder
import com.mkz.rpg.mapBuilder.domain.MapBuilderError.MapNotCreated
import com.mkz.rpg.mapBuilder.domain.MapBuilderEvent.MapResized
import com.mkz.rpg.shared.domain.FakeEventBus
import com.mkz.rpg.shared.domain.shouldHavePublishedEvents
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class ResizeMapTest {
    private val mapBuilderRepository = InMemoryMapBuilderRepository()
    private val eventBus = FakeEventBus()
    private val resizeMap = ResizeMap(mapBuilderRepository, eventBus)

    @Test
    fun `should store the resized map and announce it when the map is resized`() {
        // Given
        mapBuilderRepository.save(MapBuilder.create().pullEvents().second)
        // When
        resizeMap(rows = 3, columns = 20)
        // Then
        mapBuilderRepository.search()?.toDto()?.let { map -> map.rows to map.columns } shouldBe (3 to 20)
        eventBus.shouldHavePublishedEvents(MapResized(rows = 3, columns = 20))
    }

    @Test
    fun `should fail when the map has not been created`() {
        // When
        val resize = { resizeMap(rows = 3, columns = 3) }
        // Then
        shouldThrow<MapNotCreated> { resize() }
    }
}
