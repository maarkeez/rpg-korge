package com.mkz.rpg.mapBuilder.usecases.commands

import com.mkz.rpg.battlefield.adapters.resources.TomlBattlefieldDecoder
import com.mkz.rpg.battlefield.adapters.resources.TomlBattlefieldEncoder
import com.mkz.rpg.battlefield.usecases.queries.ParseBattlefield
import com.mkz.rpg.mapBuilder.adapters.storage.InMemoryMapBuilderRepository
import com.mkz.rpg.mapBuilder.domain.MapBuilder
import com.mkz.rpg.mapBuilder.domain.MapBuilderError.MapNotCreated
import com.mkz.rpg.mapBuilder.domain.MapBuilderEvent.MapImportRejected
import com.mkz.rpg.mapBuilder.domain.MapBuilderEvent.MapImportRejected.Reason
import com.mkz.rpg.mapBuilder.domain.MapBuilderEvent.MapImported
import com.mkz.rpg.shared.domain.FakeEventBus
import com.mkz.rpg.shared.domain.shouldHavePublishedEvents
import com.mkz.rpg.terrain.adapters.storage.InMemoryTerrainRepository
import com.mkz.rpg.terrain.domain.TerrainMother.nonOccupiableTerrain
import com.mkz.rpg.terrain.domain.TerrainMother.occupiableTerrain
import com.mkz.rpg.terrain.usecases.queries.IsTransitionAllowed
import com.mkz.rpg.terrain.usecases.queries.SearchTerrainById
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class ImportMapTest {
    private val mapBuilderRepository = InMemoryMapBuilderRepository()
    private val eventBus = FakeEventBus()

    // Only sand has transitions: to lava and water.
    private val terrainRepository =
        InMemoryTerrainRepository().apply {
            create(occupiableTerrain(id = "sand", allowedTransitionTo = setOf("sand", "lava", "water")))
            create(nonOccupiableTerrain(id = "lava", allowedTransitionTo = setOf("lava")))
            create(occupiableTerrain(id = "water", allowedTransitionTo = setOf("water")))
        }
    private val importMap =
        ImportMap(
            mapBuilderRepository,
            ParseBattlefield(TomlBattlefieldDecoder()),
            SearchTerrainById(terrainRepository),
            IsTransitionAllowed(terrainRepository),
            eventBus,
        )

    private fun givenMapCreated() = mapBuilderRepository.save(MapBuilder.create().pullEvents().second)

    private fun exported(tiles: List<List<String>>) = TomlBattlefieldEncoder().encode(tiles)

    @Test
    fun `should replace the map and announce it when an exported map is imported`() {
        // Given
        givenMapCreated()
        val tiles = listOf(listOf("lava", "sand", "water"), listOf("sand", "sand", "sand"))
        // When
        importMap(exported(tiles))
        // Then
        mapBuilderRepository.search()!!.toDto().tiles shouldBe tiles
        eventBus.shouldHavePublishedEvents(MapImported(rows = 2, columns = 3))
    }

    @Test
    fun `should reject the import when the text is not a map`() {
        // Given
        givenMapCreated()
        // When
        importMap("Hello, map builder!")
        // Then
        mapBuilderRepository.search()!!.toDto().rows shouldBe 16
        eventBus.shouldHavePublishedEvents(MapImportRejected(reason = Reason.NOT_A_MAP))
    }

    @Test
    fun `should reject the import when the map has a terrain that does not exist`() {
        // Given
        givenMapCreated()
        // When
        importMap(exported(listOf(listOf("sand", "forest"))))
        // Then
        eventBus.shouldHavePublishedEvents(MapImportRejected(reason = Reason.UNKNOWN_TERRAIN, terrainId = "forest"))
    }

    @Test
    fun `should reject the import when neither of two neighbouring terrains has a transition to the other`() {
        // Given
        givenMapCreated()
        // When
        importMap(exported(listOf(listOf("water", "lava"))))
        // Then
        eventBus.shouldHavePublishedEvents(MapImportRejected(reason = Reason.FORBIDDEN_NEIGHBOURS, terrainId = "water", neighbourTerrainId = "lava"))
    }

    @Test
    fun `should fail when the map has not been created`() {
        // When
        val import = { importMap(exported(listOf(listOf("sand")))) }
        // Then
        shouldThrow<MapNotCreated> { import() }
    }
}
