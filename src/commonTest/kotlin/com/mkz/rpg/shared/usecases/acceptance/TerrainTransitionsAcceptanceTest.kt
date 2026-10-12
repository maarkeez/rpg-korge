package com.mkz.rpg.shared.usecases.acceptance

import com.mkz.rpg.battlesetup.adapters.presentation.BattleSetupApi
import com.mkz.rpg.shared.adapters.events.InMemoryEventBus
import com.mkz.rpg.terrain.adapters.presentation.TerrainApi
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import kotlin.test.Test

class TerrainTransitionsAcceptanceTest {
    private val eventBus = InMemoryEventBus()
    private val terrainApi = TerrainApi(eventBus)
    private val battleSetupApi = BattleSetupApi(eventBus)

    private suspend fun setUp() {
        terrainApi.init()
    }

    @Test
    fun `should allow only the sand to void transition when the battle is set up`() =
        scenarioTest(::setUp) {
            // Given
            battleSetupApi.setupBattle()
            // When
            eventBus.dispatch()
            // Then
            require(terrainApi.searchTerrainById("sand") != null)
            require(terrainApi.searchTerrainById("void") != null)
            terrainApi.isTransitionAllowed(terrainId = "sand", transitionToTerrainId = "void").shouldBeTrue()
            terrainApi.isTransitionAllowed(terrainId = "void", transitionToTerrainId = "sand").shouldBeFalse()
        }
}
