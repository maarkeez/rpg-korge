package com.mkz.tessavar.worldBuilder.domain

import com.mkz.tessavar.worldBuilder.domain.WorldBuilderError.TileNotFound
import com.mkz.tessavar.worldBuilder.domain.WorldBuilderEvent.TerrainSelected
import com.mkz.tessavar.worldBuilder.domain.WorldBuilderEvent.TilePaintRejected
import com.mkz.tessavar.worldBuilder.domain.WorldBuilderEvent.TilePainted
import com.mkz.tessavar.worldBuilder.domain.WorldBuilderEvent.WorldCreated
import com.mkz.tessavar.worldBuilder.domain.WorldBuilderEvent.WorldImportRejected
import com.mkz.tessavar.worldBuilder.domain.WorldBuilderEvent.WorldImportRejected.Reason
import com.mkz.tessavar.worldBuilder.domain.WorldBuilderEvent.WorldImported
import com.mkz.tessavar.worldBuilder.domain.WorldBuilderEvent.WorldResized
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class WorldBuilderTest {
    private val canBeAdjacent = { terrainId: String, adjacentTerrainId: String -> "sand" in setOf(terrainId, adjacentTerrainId) }

    private fun newWorld(): WorldBuilder = WorldBuilder.create().pullEvents().second

    @Test
    fun `should be 16 x 16 sand tiles with sand selected when it is created`() {
        // When
        val (events, worldBuilder) = WorldBuilder.create().pullEvents()
        // Then
        worldBuilder.toDto() shouldBe WorldBuilder.Dto(rows = 16, columns = 16, tiles = List(16) { List(16) { "sand" } }, selectedTerrainId = "sand")
        events.shouldContainExactly(WorldCreated(rows = 16, columns = 16))
    }

    @Test
    fun `should keep the tiles that fit and fill the new ones with sand when it is resized`() {
        // Given
        val worldBuilder =
            newWorld()
                .selectTerrain("lava")
                .paint(row = 0, column = 1, canBeAdjacent)
                .paint(row = 1, column = 0, canBeAdjacent)
                .pullEvents()
                .second
        // When
        val (events, resized) = worldBuilder.resize(rows = 1, columns = 3).pullEvents()
        // Then
        resized.toDto().tiles shouldBe listOf(listOf("sand", "lava", "sand"))
        events.shouldContainExactly(WorldResized(rows = 1, columns = 3))
    }

    @Test
    fun `should grow up to 128 rows and columns when it is resized to the maximum size`() {
        // When
        val resized = newWorld().resize(rows = 128, columns = 128)
        // Then
        (resized.toDto().rows to resized.toDto().columns) shouldBe (128 to 128)
        resized
            .toDto()
            .tiles
            .flatten()
            .toSet() shouldBe setOf("sand")
    }

    @Test
    fun `should be ignored when it is resized out of 1 to 128 rows or columns`() {
        // Given
        val worldBuilder = newWorld()
        // When
        val resizedWorlds = listOf(worldBuilder.resize(rows = 0, columns = 16), worldBuilder.resize(rows = 16, columns = 129))
        // Then
        resizedWorlds.forEach { resized -> resized shouldBe worldBuilder }
    }

    @Test
    fun `should announce nothing when it is resized to its size`() {
        // When
        val (events, _) = newWorld().resize(rows = 16, columns = 16).pullEvents()
        // Then
        events.shouldBeEmpty()
    }

    @Test
    fun `should select the terrain the tiles are painted with when a terrain is selected`() {
        // When
        val (events, worldBuilder) = newWorld().selectTerrain("water").pullEvents()
        // Then
        worldBuilder.toDto().selectedTerrainId shouldBe "water"
        events.shouldContainExactly(TerrainSelected(terrainId = "water"))
    }

    @Test
    fun `should paint the tile with the selected terrain when its adjacent tiles can be next to it`() {
        // Given
        val worldBuilder = newWorld().selectTerrain("lava").pullEvents().second
        // When
        val (events, painted) = worldBuilder.paint(row = 2, column = 3, canBeAdjacent).pullEvents()
        // Then
        painted.toDto().tiles[2][3] shouldBe "lava"
        events.shouldContainExactly(TilePainted(row = 2, column = 3, terrainId = "lava"))
    }

    @Test
    fun `should reject the paint and keep the tile when the selected terrain can not be next to an adjacent tile`() {
        // Given
        val worldBuilder =
            newWorld()
                .selectTerrain("lava")
                .paint(row = 2, column = 3, canBeAdjacent)
                .selectTerrain("water")
                .pullEvents()
                .second
        // When
        val (events, rejected) = worldBuilder.paint(row = 2, column = 4, canBeAdjacent).pullEvents()
        // Then
        rejected.toDto().tiles[2][4] shouldBe "sand"
        events.shouldContainExactly(TilePaintRejected(row = 2, column = 4, terrainId = "water", adjacentTerrainId = "lava"))
    }

    @Test
    fun `should announce nothing when the tile already has the selected terrain`() {
        // When
        val (events, _) = newWorld().paint(row = 0, column = 0, canBeAdjacent).pullEvents()
        // Then
        events.shouldBeEmpty()
    }

    @Test
    fun `should fail when the painted tile is out of the world`() {
        // When
        val paint = { newWorld().paint(row = 16, column = 0, canBeAdjacent) }
        // Then
        shouldThrow<TileNotFound> { paint() }
    }

    private val isKnownTerrain = { terrainId: String -> terrainId in setOf("sand", "lava", "water") }

    @Test
    fun `should replace the tiles and keep the selected terrain when a world is imported`() {
        // Given
        val worldBuilder = newWorld().selectTerrain("lava").pullEvents().second
        val tiles = listOf(listOf("sand", "lava", "lava"), listOf("water", "sand", "sand"))
        // When
        val (events, imported) = worldBuilder.import(tiles, isKnownTerrain, canBeAdjacent).pullEvents()
        // Then
        imported.toDto() shouldBe WorldBuilder.Dto(rows = 2, columns = 3, tiles = tiles, selectedTerrainId = "lava")
        events.shouldContainExactly(WorldImported(rows = 2, columns = 3))
    }

    @Test
    fun `should reject the import and keep the world when the imported text is not a world`() {
        // Given
        val worldBuilder = newWorld()
        // When
        val (events, rejected) = worldBuilder.import(tiles = null, isKnownTerrain, canBeAdjacent).pullEvents()
        // Then
        rejected shouldBe worldBuilder
        events.shouldContainExactly(WorldImportRejected(reason = Reason.NOT_A_WORLD))
    }

    @Test
    fun `should reject the import when the imported world is larger than 128 rows or columns`() {
        // When
        val (events, _) = newWorld().import(List(129) { List(4) { "sand" } }, isKnownTerrain, canBeAdjacent).pullEvents()
        // Then
        events.shouldContainExactly(WorldImportRejected(reason = Reason.SIZE_OUT_OF_RANGE))
    }

    @Test
    fun `should reject the import when the imported world has no columns`() {
        // When
        val (events, _) = newWorld().import(listOf(emptyList()), isKnownTerrain, canBeAdjacent).pullEvents()
        // Then
        events.shouldContainExactly(WorldImportRejected(reason = Reason.SIZE_OUT_OF_RANGE))
    }

    @Test
    fun `should reject the import with the terrain when the imported world has an unknown terrain`() {
        // When
        val (events, _) = newWorld().import(listOf(listOf("sand", "forest")), isKnownTerrain, canBeAdjacent).pullEvents()
        // Then
        events.shouldContainExactly(WorldImportRejected(reason = Reason.UNKNOWN_TERRAIN, terrainId = "forest"))
    }

    @Test
    fun `should reject the import with the terrains when two adjacent tiles of the imported world can not be next to each other`() {
        // Given
        val tiles = listOf(listOf("lava", "sand"), listOf("water", "sand"))
        // When
        val (events, rejected) = newWorld().import(tiles, isKnownTerrain, canBeAdjacent).pullEvents()
        // Then
        events.shouldContainExactly(WorldImportRejected(reason = Reason.FORBIDDEN_ADJACENT_TERRAINS, terrainId = "lava", adjacentTerrainId = "water"))
        rejected.toDto().rows shouldBe 16
    }
}
