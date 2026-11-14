package com.mkz.tessavar.shared.usecases.acceptance

import com.mkz.tessavar.battlefield.adapters.presentation.BattlefieldApi
import com.mkz.tessavar.battlefield.adapters.resources.ResourcesBattlefieldLoader
import com.mkz.tessavar.battlefield.adapters.resources.TomlBattlefieldDecoder
import com.mkz.tessavar.battlefield.adapters.resources.TomlBattlefieldEncoder
import com.mkz.tessavar.shared.adapters.events.InMemoryEventBus
import com.mkz.tessavar.terrain.adapters.presentation.TerrainApi
import com.mkz.tessavar.terrain.adapters.resources.ResourcesTerrainLoader
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.maps.shouldHaveSize
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class BattlefieldTerrainTransitionsAcceptanceTest {
    private val eventBus = InMemoryEventBus()
    private val terrainApi = TerrainApi(eventBus, ResourcesTerrainLoader())
    private val battlefieldApi = BattlefieldApi(terrainApi, eventBus, ResourcesBattlefieldLoader(), TomlBattlefieldEncoder(), TomlBattlefieldDecoder())

    private suspend fun setUp() {
        terrainApi.init()
        terrainApi.initialiseTerrains()
    }

    @Test
    fun `should initialize battlefield when the tile terrain transitions are allowed`() =
        scenarioTest(::setUp) {
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
            battlefieldApi.requestBattlefieldCreation(rows = rows, columns = columns, tiles = tiles)
            // Then
            val battlefield = battlefieldApi.searchBattlefield()
            battlefield.shouldNotBeNull()
            battlefield!!.rows shouldBe rows
            battlefield.columns shouldBe columns
            battlefield.tiles.shouldHaveSize(rows * columns)
        }
}
