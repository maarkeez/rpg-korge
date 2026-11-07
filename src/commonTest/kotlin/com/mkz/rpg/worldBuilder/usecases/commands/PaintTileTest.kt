package com.mkz.rpg.worldBuilder.usecases.commands

import com.mkz.rpg.shared.domain.FakeEventBus
import com.mkz.rpg.shared.domain.shouldHavePublishedEvents
import com.mkz.rpg.terrain.adapters.storage.InMemoryTerrainRepository
import com.mkz.rpg.terrain.domain.TerrainMother.nonOccupiableTerrain
import com.mkz.rpg.terrain.domain.TerrainMother.occupiableTerrain
import com.mkz.rpg.terrain.usecases.queries.IsTransitionAllowed
import com.mkz.rpg.worldBuilder.adapters.storage.InMemoryWorldBuilderRepository
import com.mkz.rpg.worldBuilder.domain.WorldBuilder
import com.mkz.rpg.worldBuilder.domain.WorldBuilderError.WorldNotCreated
import com.mkz.rpg.worldBuilder.domain.WorldBuilderEvent.TilePaintRejected
import com.mkz.rpg.worldBuilder.domain.WorldBuilderEvent.TilePainted
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class PaintTileTest {
    private val worldBuilderRepository = InMemoryWorldBuilderRepository()
    private val eventBus = FakeEventBus()

    // Only sand has transitions: to lava and water.
    private val terrainRepository =
        InMemoryTerrainRepository().apply {
            create(occupiableTerrain(id = "sand", allowedTransitionTo = setOf("sand", "lava", "water")))
            create(nonOccupiableTerrain(id = "lava", allowedTransitionTo = setOf("lava")))
            create(occupiableTerrain(id = "water", allowedTransitionTo = setOf("water")))
        }
    private val paintTile = PaintTile(worldBuilderRepository, IsTransitionAllowed(terrainRepository), eventBus)

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
