package com.mkz.rpg.mapBuilder.usecases.commands

import com.mkz.rpg.mapBuilder.adapters.storage.InMemoryMapBuilderRepository
import com.mkz.rpg.mapBuilder.domain.MapBuilder
import com.mkz.rpg.mapBuilder.domain.MapBuilderError.MapNotCreated
import com.mkz.rpg.mapBuilder.domain.MapBuilderEvent.TilePaintRejected
import com.mkz.rpg.mapBuilder.domain.MapBuilderEvent.TilePainted
import com.mkz.rpg.shared.domain.FakeEventBus
import com.mkz.rpg.shared.domain.shouldHavePublishedEvents
import com.mkz.rpg.terrain.adapters.storage.InMemoryTerrainRepository
import com.mkz.rpg.terrain.domain.TerrainMother.nonOccupiableTerrain
import com.mkz.rpg.terrain.domain.TerrainMother.occupiableTerrain
import com.mkz.rpg.terrain.usecases.queries.IsTransitionAllowed
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class PaintTileTest {
    private val mapBuilderRepository = InMemoryMapBuilderRepository()
    private val eventBus = FakeEventBus()

    // Only sand has transitions: to lava and water.
    private val terrainRepository =
        InMemoryTerrainRepository().apply {
            create(occupiableTerrain(id = "sand", allowedTransitionTo = setOf("sand", "lava", "water")))
            create(nonOccupiableTerrain(id = "lava", allowedTransitionTo = setOf("lava")))
            create(occupiableTerrain(id = "water", allowedTransitionTo = setOf("water")))
        }
    private val paintTile = PaintTile(mapBuilderRepository, IsTransitionAllowed(terrainRepository), eventBus)

    private fun givenMap(vararg paintedTiles: Triple<Int, Int, String>) {
        var mapBuilder = MapBuilder.create()
        paintedTiles.forEach { (row, column, terrainId) -> mapBuilder = mapBuilder.selectTerrain(terrainId).paint(row, column) { _, _ -> true } }
        mapBuilderRepository.save(mapBuilder.pullEvents().second)
    }

    private fun select(terrainId: String) =
        mapBuilderRepository.save(
            mapBuilderRepository
                .search()!!
                .selectTerrain(terrainId)
                .pullEvents()
                .second,
        )

    @Test
    fun `should paint the tile when the terrain has a transition to its neighbours`() {
        // Given
        givenMap(Triple(0, 0, "lava"), Triple(0, 1, "lava"))
        select("sand")
        // When
        paintTile(row = 0, column = 1)
        // Then
        mapBuilderRepository.search()!!.toDto().tiles[0][1] shouldBe "sand"
        eventBus.shouldHavePublishedEvents(TilePainted(row = 0, column = 1, terrainId = "sand"))
    }

    @Test
    fun `should paint the tile when its neighbours have a transition to the terrain`() {
        // Given
        givenMap()
        select("lava")
        // When
        paintTile(row = 0, column = 0)
        // Then
        mapBuilderRepository.search()!!.toDto().tiles[0][0] shouldBe "lava"
        eventBus.shouldHavePublishedEvents(TilePainted(row = 0, column = 0, terrainId = "lava"))
    }

    @Test
    fun `should reject the paint when neither terrain has a transition to the other`() {
        // Given
        givenMap(Triple(0, 0, "lava"))
        select("water")
        // When
        paintTile(row = 0, column = 1)
        // Then
        mapBuilderRepository.search()!!.toDto().tiles[0][1] shouldBe "sand"
        eventBus.shouldHavePublishedEvents(TilePaintRejected(row = 0, column = 1, terrainId = "water", neighbourTerrainId = "lava"))
    }

    @Test
    fun `should fail when the map has not been created`() {
        // When
        val paint = { paintTile(row = 0, column = 0) }
        // Then
        shouldThrow<MapNotCreated> { paint() }
    }
}
