package com.mkz.rpg.battlefield.usecases.commands

import com.mkz.rpg.battlefield.adapters.storage.InMemoryBattlefieldRepository
import com.mkz.rpg.battlefield.domain.BattlefieldError
import com.mkz.rpg.battlefield.domain.BattlefieldEvent
import com.mkz.rpg.battlefield.domain.BattlefieldMother
import com.mkz.rpg.battlefield.domain.BattlefieldMother.terrainId
import com.mkz.rpg.battlefield.usecases.services.BattlefieldCreation
import com.mkz.rpg.shared.domain.FakeEventBus
import com.mkz.rpg.shared.domain.shouldHavePublishedEvents
import com.mkz.rpg.terrain.adapters.storage.InMemoryTerrainRepository
import com.mkz.rpg.terrain.domain.TerrainMother
import com.mkz.rpg.terrain.usecases.queries.SearchTerrainById
import io.kotest.assertions.throwables.shouldThrowAny
import io.kotest.inspectors.forAll
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.maps.shouldHaveSize
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class RequestBattlefieldCreationTest {
    private val battlefieldRepository = InMemoryBattlefieldRepository()
    private val terrainRepository = InMemoryTerrainRepository()
    private val eventBus = FakeEventBus()
    private val requestBattlefieldCreation =
        RequestBattlefieldCreation(
            battlefieldCreation = BattlefieldCreation(battlefieldRepository, SearchTerrainById(terrainRepository)),
            eventBus = eventBus,
        )

    @Test
    fun `should create battlefield when no battlefield exists and tile terrain transitions are allowed`() {
        // Given
        val rows = 3
        val columns = 4
        val tiles =
            List(rows) {
                List(columns) {
                    terrainId()
                }
            }
        terrainRepository.create(TerrainMother.occupiableTerrain(id = "sand", allowedTransitionTo = setOf("sand", "void")))
        terrainRepository.create(TerrainMother.occupiableTerrain(id = "void", allowedTransitionTo = setOf("sand", "void")))
        // When
        requestBattlefieldCreation(rows = rows, columns = columns, tiles = tiles)
        // Then
        val storedBattlefield = battlefieldRepository.search()?.toDto()
        storedBattlefield.shouldNotBeNull()
        storedBattlefield!!.rows shouldBe rows
        storedBattlefield.columns shouldBe columns
        storedBattlefield.tiles.shouldHaveSize(rows * columns)
        storedBattlefield.tiles.values.forAll { tile -> tile.occupantId.shouldBeNull() }
        eventBus.shouldHavePublishedEvents(BattlefieldEvent.BattlefieldCreated)
    }

    @Test
    fun `should not create battlefield when a battlefield already exists`() {
        // Given
        val existingBattlefield =
            BattlefieldMother
                .battlefield()
        battlefieldRepository.create(existingBattlefield)
        // When
        requestBattlefieldCreation(rows = 3, columns = 3, tiles = emptyList())
        // Then
        battlefieldRepository.search()?.toDto() shouldBe existingBattlefield.toDto()
        eventBus.publishedEvents.shouldBeEmpty()
    }

    @Test
    fun `should throw BattlefieldError when a tile terrain transition is not allowed`() {
        // Given
        val rows = 1
        val columns = 2
        val tiles =
            listOf(
                listOf("sand", "void"),
            )
        terrainRepository.create(TerrainMother.occupiableTerrain(id = "sand"))
        terrainRepository.create(TerrainMother.occupiableTerrain(id = "void"))
        // When
        val error = shouldThrowAny { requestBattlefieldCreation(rows = rows, columns = columns, tiles = tiles) }
        // Then
        val tileTerrainTransitionNotAllowed = error as BattlefieldError.TileTerrainTransitionNotAllowed
        tileTerrainTransitionNotAllowed.terrainId shouldBe "sand"
        tileTerrainTransitionNotAllowed.transitionToTerrainId shouldBe "void"
        battlefieldRepository.search().shouldBeNull()
        eventBus.publishedEvents.shouldBeEmpty()
    }

    @Test
    fun `should create battlefield tiles that can not be occupied when the tile terrain can not be occupied`() {
        // Given
        val rows = 1
        val columns = 2
        val tiles =
            listOf(
                listOf("void", "sand"),
            )
        terrainRepository.create(TerrainMother.nonOccupiableTerrain(id = "void"))
        terrainRepository.create(TerrainMother.occupiableTerrain(id = "sand", allowedTransitionTo = setOf("sand", "void")))
        // When
        requestBattlefieldCreation(rows = rows, columns = columns, tiles = tiles)
        // Then
        val storedBattlefield = battlefieldRepository.search()
        storedBattlefield.shouldNotBeNull()
        storedBattlefield!!.canBeOccupied(row = 0, column = 0).shouldBeFalse()
        storedBattlefield.canBeOccupied(row = 0, column = 1).shouldBeTrue()
    }
}
