package com.mkz.rpg.mapBuilder.usecases.queries

import com.mkz.rpg.mapBuilder.adapters.storage.InMemoryMapBuilderRepository
import com.mkz.rpg.mapBuilder.domain.MapBuilder
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class SearchMapTest {
    private val mapBuilderRepository = InMemoryMapBuilderRepository()
    private val searchMap = SearchMap(mapBuilderRepository)

    @Test
    fun `should return the map when it has been created`() {
        // Given
        val mapBuilder = MapBuilder.create()
        mapBuilderRepository.save(mapBuilder)
        // When
        val map = searchMap()
        // Then
        map shouldBe mapBuilder.toDto()
    }

    @Test
    fun `should return null when the map has not been created`() {
        // When
        val map = searchMap()
        // Then
        map.shouldBeNull()
    }
}
