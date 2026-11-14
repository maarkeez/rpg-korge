package com.mkz.tessavar.worldBuilder.usecases.commands

import com.mkz.tessavar.shared.domain.FakeEventBus
import com.mkz.tessavar.shared.domain.shouldHavePublishedEvents
import com.mkz.tessavar.terrain.adapters.storage.InMemoryTerrainRepository
import com.mkz.tessavar.terrain.domain.TerrainMother.terrainThatCanBeOccupied
import com.mkz.tessavar.terrain.domain.TerrainMother.terrainThatCanNotBeOccupied
import com.mkz.tessavar.terrain.usecases.queries.HasTerrainTransition
import com.mkz.tessavar.worldBuilder.adapters.storage.InMemoryWorldBuilderRepository
import com.mkz.tessavar.worldBuilder.domain.WorldBuilder
import com.mkz.tessavar.worldBuilder.domain.WorldBuilderError.WorldNotCreated
import com.mkz.tessavar.worldBuilder.domain.WorldBuilderEvent.TilePaintRejected
import com.mkz.tessavar.worldBuilder.domain.WorldBuilderEvent.TilePainted
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class PaintTileTest {
    private val worldBuilderRepository = InMemoryWorldBuilderRepository()
    private val eventBus = FakeEventBus()

    private val terrainRepository =
        InMemoryTerrainRepository().apply {
            create(terrainThatCanBeOccupied(id = "sand", transitionsTo = setOf("sand", "lava", "water")))
            create(terrainThatCanNotBeOccupied(id = "lava", transitionsTo = setOf("lava")))
            create(terrainThatCanBeOccupied(id = "water", transitionsTo = setOf("water")))
        }
    private val paintTile = PaintTile(worldBuilderRepository, HasTerrainTransition(terrainRepository), eventBus)

    private fun givenWorld(vararg paintedTiles: Triple<Int, Int, String>) {
        var worldBuilder = WorldBuilder.create()
        paintedTiles.forEach { (row, column, terrainId) -> worldBuilder = worldBuilder.selectTerrain(terrainId).paint(row, column) { _, _ -> true } }
        worldBuilderRepository.save(worldBuilder.pullEvents().second)
    }

    private fun select(terrainId: String) =
        worldBuilderRepository.save(
            worldBuilderRepository
                .search()!!
                .selectTerrain(terrainId)
                .pullEvents()
                .second,
        )

    @Test
    fun `should paint the tile when the terrain has a transition to its adjacent tiles`() {
        // Given
        givenWorld(Triple(0, 0, "lava"), Triple(0, 1, "lava"))
        select("sand")
        // When
        paintTile(row = 0, column = 1)
        // Then
        worldBuilderRepository.search()!!.toDto().tiles[0][1] shouldBe "sand"
        eventBus.shouldHavePublishedEvents(TilePainted(row = 0, column = 1, terrainId = "sand"))
    }

    @Test
    fun `should paint the tile when its adjacent tiles have a transition to the terrain`() {
        // Given
        givenWorld()
        select("lava")
        // When
        paintTile(row = 0, column = 0)
        // Then
        worldBuilderRepository.search()!!.toDto().tiles[0][0] shouldBe "lava"
        eventBus.shouldHavePublishedEvents(TilePainted(row = 0, column = 0, terrainId = "lava"))
    }

    @Test
    fun `should reject the paint when neither terrain has a transition to the other`() {
        // Given
        givenWorld(Triple(0, 0, "lava"))
        select("water")
        // When
        paintTile(row = 0, column = 1)
        // Then
        worldBuilderRepository.search()!!.toDto().tiles[0][1] shouldBe "sand"
        eventBus.shouldHavePublishedEvents(TilePaintRejected(row = 0, column = 1, terrainId = "water", adjacentTerrainId = "lava"))
    }

    @Test
    fun `should fail when the world has not been created`() {
        // When
        val paint = { paintTile(row = 0, column = 0) }
        // Then
        shouldThrow<WorldNotCreated> { paint() }
    }
}
