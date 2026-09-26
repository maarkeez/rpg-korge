package com.mkz.rpg.shared.usecases.acceptance

import com.mkz.rpg.battlesetup.adapters.presentation.BattleSetupApi
import com.mkz.rpg.shared.adapters.events.InMemoryEventBus
import com.mkz.rpg.terrain.adapters.presentation.TerrainApi
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class TerrainTransitionsAcceptanceTest {
    private val eventBus = InMemoryEventBus()
    private val terrainApi = TerrainApi(eventBus)
    private val battleSetupApi = BattleSetupApi(eventBus)

    @Test
    fun `should allow the sand to void transition when the battle is set up`() {
        // Given
        battleSetupApi.setupBattle()
        // When
        eventBus.dispatch()
        // Then
        require(terrainApi.searchTerrainById("sand") != null)
        require(terrainApi.searchTerrainById("void") != null)
        assertThat(terrainApi.isTransitionAllowed(terrainId = "sand", transitionToTerrainId = "void")).isTrue()
        assertThat(terrainApi.isTransitionAllowed(terrainId = "void", transitionToTerrainId = "sand")).isTrue()
    }
}
