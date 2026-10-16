package com.mkz.rpg.mapBuilder.domain

import com.mkz.rpg.mapBuilder.domain.MapBuilderError.TileNotFound
import com.mkz.rpg.mapBuilder.domain.MapBuilderEvent.MapCreated
import com.mkz.rpg.mapBuilder.domain.MapBuilderEvent.MapResized
import com.mkz.rpg.mapBuilder.domain.MapBuilderEvent.TerrainSelected
import com.mkz.rpg.mapBuilder.domain.MapBuilderEvent.TilePaintRejected
import com.mkz.rpg.mapBuilder.domain.MapBuilderEvent.TilePainted
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class MapBuilderTest {
    // Lava and water can only be next to sand, and to themselves.
    private val canBeNeighbours = { terrainId: String, neighbourTerrainId: String -> "sand" in setOf(terrainId, neighbourTerrainId) }

    private fun newMap(): MapBuilder = MapBuilder.create().pullEvents().second

    @Test
    fun `should be 16 x 16 sand tiles with sand selected when it is created`() {
        // When
        val (events, mapBuilder) = MapBuilder.create().pullEvents()
        // Then
        mapBuilder.toDto() shouldBe MapBuilder.Dto(rows = 16, columns = 16, tiles = List(16) { List(16) { "sand" } }, selectedTerrainId = "sand")
        events.shouldContainExactly(MapCreated(rows = 16, columns = 16))
    }

    @Test
    fun `should keep the tiles that fit and fill the new ones with sand when it is resized`() {
        // Given
        val mapBuilder =
            newMap()
                .selectTerrain("lava")
                .paint(row = 0, column = 1, canBeNeighbours)
                .paint(row = 1, column = 0, canBeNeighbours)
                .pullEvents()
                .second
        // When
        val (events, resized) = mapBuilder.resize(rows = 1, columns = 3).pullEvents()
        // Then
        resized.toDto().tiles shouldBe listOf(listOf("sand", "lava", "sand"))
        events.shouldContainExactly(MapResized(rows = 1, columns = 3))
    }

    @Test
    fun `should grow up to 32 rows and columns when it is resized to the maximum size`() {
        // When
        val resized = newMap().resize(rows = 32, columns = 32)
        // Then
        (resized.toDto().rows to resized.toDto().columns) shouldBe (32 to 32)
        resized
            .toDto()
            .tiles
            .flatten()
            .toSet() shouldBe setOf("sand")
    }

    @Test
    fun `should be ignored when it is resized out of 1 to 32 rows or columns`() {
        // Given
        val mapBuilder = newMap()
        // When
        val resizedMaps = listOf(mapBuilder.resize(rows = 0, columns = 16), mapBuilder.resize(rows = 16, columns = 33))
        // Then
        resizedMaps.forEach { resized -> resized shouldBe mapBuilder }
    }

    @Test
    fun `should announce nothing when it is resized to its size`() {
        // When
        val (events, _) = newMap().resize(rows = 16, columns = 16).pullEvents()
        // Then
        events.shouldBeEmpty()
    }

    @Test
    fun `should select the terrain the tiles are painted with when a terrain is selected`() {
        // When
        val (events, mapBuilder) = newMap().selectTerrain("water").pullEvents()
        // Then
        mapBuilder.toDto().selectedTerrainId shouldBe "water"
        events.shouldContainExactly(TerrainSelected(terrainId = "water"))
    }

    @Test
    fun `should paint the tile with the selected terrain when its neighbours can be next to it`() {
        // Given
        val mapBuilder = newMap().selectTerrain("lava").pullEvents().second
        // When
        val (events, painted) = mapBuilder.paint(row = 2, column = 3, canBeNeighbours).pullEvents()
        // Then
        painted.toDto().tiles[2][3] shouldBe "lava"
        events.shouldContainExactly(TilePainted(row = 2, column = 3, terrainId = "lava"))
    }

    @Test
    fun `should reject the paint and keep the tile when the selected terrain can not be next to a neighbour`() {
        // Given
        val mapBuilder =
            newMap()
                .selectTerrain("lava")
                .paint(row = 2, column = 3, canBeNeighbours)
                .selectTerrain("water")
                .pullEvents()
                .second
        // When
        val (events, rejected) = mapBuilder.paint(row = 2, column = 4, canBeNeighbours).pullEvents()
        // Then
        rejected.toDto().tiles[2][4] shouldBe "sand"
        events.shouldContainExactly(TilePaintRejected(row = 2, column = 4, terrainId = "water", neighbourTerrainId = "lava"))
    }

    @Test
    fun `should announce nothing when the tile already has the selected terrain`() {
        // When
        val (events, _) = newMap().paint(row = 0, column = 0, canBeNeighbours).pullEvents()
        // Then
        events.shouldBeEmpty()
    }

    @Test
    fun `should fail when the painted tile is out of the map`() {
        // When
        val paint = { newMap().paint(row = 16, column = 0, canBeNeighbours) }
        // Then
        shouldThrow<TileNotFound> { paint() }
    }
}
