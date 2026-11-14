package com.mkz.tessavar.worldBuilder.usecases.commands

import com.mkz.tessavar.battlefield.adapters.resources.TomlBattlefieldDecoder
import com.mkz.tessavar.battlefield.adapters.resources.TomlBattlefieldEncoder
import com.mkz.tessavar.battlefield.usecases.queries.ParseBattlefield
import com.mkz.tessavar.shared.domain.FakeEventBus
import com.mkz.tessavar.shared.domain.shouldHavePublishedEvents
import com.mkz.tessavar.terrain.adapters.storage.InMemoryTerrainRepository
import com.mkz.tessavar.terrain.domain.TerrainMother.terrainThatCanBeOccupied
import com.mkz.tessavar.terrain.domain.TerrainMother.terrainThatCanNotBeOccupied
import com.mkz.tessavar.terrain.usecases.queries.HasTerrainTransition
import com.mkz.tessavar.terrain.usecases.queries.SearchTerrainById
import com.mkz.tessavar.worldBuilder.adapters.storage.InMemoryWorldBuilderRepository
import com.mkz.tessavar.worldBuilder.domain.WorldBuilder
import com.mkz.tessavar.worldBuilder.domain.WorldBuilderError.WorldNotCreated
import com.mkz.tessavar.worldBuilder.domain.WorldBuilderEvent.WorldImportRejected
import com.mkz.tessavar.worldBuilder.domain.WorldBuilderEvent.WorldImportRejected.Reason
import com.mkz.tessavar.worldBuilder.domain.WorldBuilderEvent.WorldImported
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class ImportWorldTest {
    private val worldBuilderRepository = InMemoryWorldBuilderRepository()
    private val eventBus = FakeEventBus()

    private val terrainRepository =
        InMemoryTerrainRepository().apply {
            create(terrainThatCanBeOccupied(id = "sand", transitionsTo = setOf("sand", "lava", "water")))
            create(terrainThatCanNotBeOccupied(id = "lava", transitionsTo = setOf("lava")))
            create(terrainThatCanBeOccupied(id = "water", transitionsTo = setOf("water")))
        }
    private val importWorld =
        ImportWorld(
            worldBuilderRepository,
            ParseBattlefield(TomlBattlefieldDecoder()),
            SearchTerrainById(terrainRepository),
            HasTerrainTransition(terrainRepository),
            eventBus,
        )

    private fun givenWorldCreated() = worldBuilderRepository.save(WorldBuilder.create().pullEvents().second)

    private fun exported(tiles: List<List<String>>) = TomlBattlefieldEncoder().encode(tiles)

    @Test
    fun `should replace the world and announce it when an exported world is imported`() {
        // Given
        givenWorldCreated()
        val tiles = listOf(listOf("lava", "sand", "water"), listOf("sand", "sand", "sand"))
        // When
        importWorld(exported(tiles))
        // Then
        worldBuilderRepository.search()!!.toDto().tiles shouldBe tiles
        eventBus.shouldHavePublishedEvents(WorldImported(rows = 2, columns = 3))
    }

    @Test
    fun `should reject the import when the text is not a world`() {
        // Given
        givenWorldCreated()
        // When
        importWorld("Hello, world builder!")
        // Then
        worldBuilderRepository.search()!!.toDto().rows shouldBe 16
        eventBus.shouldHavePublishedEvents(WorldImportRejected(reason = Reason.NOT_A_WORLD))
    }

    @Test
    fun `should reject the import when the world has a terrain that does not exist`() {
        // Given
        givenWorldCreated()
        // When
        importWorld(exported(listOf(listOf("sand", "forest"))))
        // Then
        eventBus.shouldHavePublishedEvents(WorldImportRejected(reason = Reason.UNKNOWN_TERRAIN, terrainId = "forest"))
    }

    @Test
    fun `should reject the import when neither of two adjacent terrains has a transition to the other`() {
        // Given
        givenWorldCreated()
        // When
        importWorld(exported(listOf(listOf("water", "lava"))))
        // Then
        eventBus.shouldHavePublishedEvents(WorldImportRejected(reason = Reason.FORBIDDEN_ADJACENT_TERRAINS, terrainId = "water", adjacentTerrainId = "lava"))
    }

    @Test
    fun `should fail when the world has not been created`() {
        // When
        val import = { importWorld(exported(listOf(listOf("sand")))) }
        // Then
        shouldThrow<WorldNotCreated> { import() }
    }
}
