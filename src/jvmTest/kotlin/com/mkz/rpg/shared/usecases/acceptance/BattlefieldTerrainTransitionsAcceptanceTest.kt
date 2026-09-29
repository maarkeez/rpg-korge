package com.mkz.rpg.shared.usecases.acceptance

import com.mkz.rpg.battlefield.adapters.presentation.BattlefieldApi
import com.mkz.rpg.shared.adapters.events.InMemoryEventBus
import com.mkz.rpg.terrain.adapters.presentation.TerrainApi
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class BattlefieldTerrainTransitionsAcceptanceTest {
    private val eventBus = InMemoryEventBus()
    private val terrainApi = TerrainApi(eventBus)
    private val battlefieldApi = BattlefieldApi(terrainApi, eventBus)

    @BeforeEach
    fun setup() {
        runBlocking { terrainApi.init() }
        terrainApi.initialiseTerrains()
    }

    @Test
    fun `should initialize battlefield when the tile terrain transitions are allowed`() {
        // Given
        val rows = 3
        val columns = 3
        val tiles =
            listOf(
                listOf("sand", "void", "sand"),
                listOf("void", "sand", "void"),
                listOf("sand", "void", "sand"),
            )
        // When
        battlefieldApi.initializeBattlefield(rows = rows, columns = columns, tiles = tiles)
        // Then
        val battlefield = battlefieldApi.searchBattlefield()
        assertThat(battlefield).isNotNull
        assertThat(battlefield!!.rows).isEqualTo(rows)
        assertThat(battlefield.columns).isEqualTo(columns)
        assertThat(battlefield.tiles).hasSize(rows * columns)
    }
}
