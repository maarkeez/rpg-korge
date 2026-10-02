package com.mkz.rpg.battlefield.usecases.commands

import com.mkz.rpg.battlefield.adapters.storage.InMemoryBattlefieldRepository
import com.mkz.rpg.battlefield.domain.BattlefieldError
import com.mkz.rpg.battlefield.domain.BattlefieldEvent
import com.mkz.rpg.battlefield.domain.BattlefieldMother
import com.mkz.rpg.battlefield.domain.BattlefieldMother.terrainId
import com.mkz.rpg.shared.domain.FakeEventBus
import com.mkz.rpg.shared.domain.assertThat
import com.mkz.rpg.terrain.domain.TerrainMother
import com.mkz.rpg.terrain.usecases.queries.SearchTerrainById
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.catchThrowable
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class InitializeBattlefieldTest {
    private val battlefieldRepository = InMemoryBattlefieldRepository()
    private val searchTerrainById: SearchTerrainById = mock()
    private val eventBus = FakeEventBus()
    private val initializeBattlefield =
        InitializeBattlefield(
            battlefieldRepository = battlefieldRepository,
            searchTerrainById = searchTerrainById,
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
        whenever(searchTerrainById(any())).thenReturn(TerrainMother.occupiableTerrain(allowedTransitionTo = setOf("sand", "void")).toDto())
        // When
        initializeBattlefield(rows = rows, columns = columns, tiles = tiles)
        // Then
        val storedBattlefield = battlefieldRepository.search()?.toDto()
        assertThat(storedBattlefield).isNotNull
        assertThat(storedBattlefield!!.rows).isEqualTo(rows)
        assertThat(storedBattlefield.columns).isEqualTo(columns)
        assertThat(storedBattlefield.tiles).hasSize(rows * columns)
        assertThat(storedBattlefield.tiles.values)
            .allSatisfy { tile -> assertThat(tile.battleUnitId).isNull() }
        assertThat(eventBus)
            .hasPublishedEvents(BattlefieldEvent.BattlefieldCreated)
    }

    @Test
    fun `should not create battlefield when a battlefield already exists`() {
        // Given
        val existingBattlefield =
            BattlefieldMother
                .battlefield()
        battlefieldRepository.create(existingBattlefield)
        // When
        initializeBattlefield(rows = 3, columns = 3, tiles = emptyList())
        // Then
        assertThat(battlefieldRepository.search()?.toDto())
            .isEqualTo(existingBattlefield.toDto())
        assertThat(eventBus.publishedEvents).isEmpty()
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
        whenever(searchTerrainById(any())).thenReturn(TerrainMother.occupiableTerrain().toDto())
        // When
        val error = catchThrowable { initializeBattlefield(rows = rows, columns = columns, tiles = tiles) }
        // Then
        val tileTerrainTransitionNotAllowed = error as BattlefieldError.TileTerrainTransitionNotAllowed
        assertThat(tileTerrainTransitionNotAllowed.terrainId).isEqualTo("sand")
        assertThat(tileTerrainTransitionNotAllowed.transitionToTerrainId).isEqualTo("void")
        assertThat(battlefieldRepository.search()).isNull()
        assertThat(eventBus.publishedEvents).isEmpty()
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
        whenever(searchTerrainById("void")).thenReturn(TerrainMother.nonOccupiableTerrain(id = "void").toDto())
        whenever(searchTerrainById("sand")).thenReturn(
            TerrainMother
                .occupiableTerrain(id = "sand", allowedTransitionTo = setOf("sand", "void"))
                .toDto(),
        )
        // When
        initializeBattlefield(rows = rows, columns = columns, tiles = tiles)
        // Then
        val storedBattlefield = battlefieldRepository.search()
        assertThat(storedBattlefield).isNotNull
        assertThat(storedBattlefield!!.canBeOccupied(row = 0, column = 0)).isFalse()
        assertThat(storedBattlefield.canBeOccupied(row = 0, column = 1)).isTrue()
    }
}
