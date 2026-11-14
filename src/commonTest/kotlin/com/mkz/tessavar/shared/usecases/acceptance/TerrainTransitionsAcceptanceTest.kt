package com.mkz.tessavar.shared.usecases.acceptance

import com.mkz.tessavar.gameSetup.adapters.presentation.GameSetupApi
import com.mkz.tessavar.shared.adapters.events.InMemoryEventBus
import com.mkz.tessavar.terrain.adapters.presentation.TerrainApi
import com.mkz.tessavar.terrain.adapters.resources.ResourcesTerrainLoader
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import kotlin.test.Test

class TerrainTransitionsAcceptanceTest {
    private val eventBus = InMemoryEventBus()
    private val terrainApi = TerrainApi(eventBus, ResourcesTerrainLoader())
    private val gameSetupApi = GameSetupApi(eventBus)

    private suspend fun setUp() {
        terrainApi.init()
    }

    @Test
    fun `should have only the sand to void transition when the game is set up`() =
        scenarioTest(::setUp) {
            // Given
            gameSetupApi.setUpGame()
            // When
            eventBus.dispatch()
            // Then
            require(terrainApi.searchTerrainById("sand") != null)
            require(terrainApi.searchTerrainById("void") != null)
            terrainApi.hasTerrainTransition(terrainId = "sand", toTerrainId = "void").shouldBeTrue()
            terrainApi.hasTerrainTransition(terrainId = "void", toTerrainId = "sand").shouldBeFalse()
        }
}
