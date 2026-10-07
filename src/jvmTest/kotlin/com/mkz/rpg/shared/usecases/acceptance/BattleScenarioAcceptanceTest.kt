package com.mkz.rpg.shared.usecases.acceptance

import com.mkz.rpg.ability.adapters.presentation.AbilityApi
import com.mkz.rpg.battle.adapters.presentation.BattleApi
import com.mkz.rpg.battleUnit.adapters.presentation.BattleUnitApi
import com.mkz.rpg.battlefield.adapters.presentation.BattlefieldApi
import com.mkz.rpg.battlefield.domain.Battlefield
import com.mkz.rpg.battlesetup.adapters.presentation.BattleSetupApi
import com.mkz.rpg.battlesetup.adapters.serialization.BattleScenarioLoader
import com.mkz.rpg.battlesetup.domain.Battlesetup
import com.mkz.rpg.cpuBrain.adapters.presentation.CpuBrainApi
import com.mkz.rpg.effect.adapters.presentation.EffectApi
import com.mkz.rpg.player.adapters.presentation.PlayerApi
import com.mkz.rpg.shared.adapters.events.InMemoryEventBus
import com.mkz.rpg.terrain.adapters.presentation.TerrainApi
import com.mkz.rpg.unit.adapters.presentation.UnitApi
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import kotlin.random.Random

class BattleScenarioAcceptanceTest {
    private class ScenarioGraph(
        seed: Long,
    ) {
        val eventBus = InMemoryEventBus()
        val random = Random(seed)
        val terrainApi = TerrainApi(eventBus)
        val unitApi = UnitApi(eventBus)
        val playerApi = PlayerApi(eventBus)
        val battlefieldApi = BattlefieldApi(terrainApi, eventBus)
        val effectApi = EffectApi(eventBus)
        val abilityApi = AbilityApi(effectApi, eventBus)
        val battleUnitApi = BattleUnitApi(effectApi, abilityApi, unitApi, playerApi, battlefieldApi, eventBus, random)
        val battleApi = BattleApi(eventBus, battleUnitApi)
        val cpuBrainApi = CpuBrainApi(unitApi, playerApi, battleUnitApi, battlefieldApi, terrainApi, effectApi, eventBus, random)
        val battleSetupApi = BattleSetupApi(eventBus)

        suspend fun setupBattle(scenario: Battlesetup? = null) {
            terrainApi.init()
            battleSetupApi.setupBattle(scenario)
            eventBus.dispatch()
        }
    }

    @Test
    fun `should start the battle with the deployed units on the scenario tiles when a scenario is set up`() {
        // Given
        val loader = BattleScenarioLoader()
        val starterScenarios =
            listOf(
                "scenarios/default.json",
                "scenarios/chain-showcase.json",
                "scenarios/terrain-mix.json",
            )
        // When
        val graphs =
            starterScenarios.associateWith { path ->
                val scenario = runBlocking { loader.load(path) }
                val graph = ScenarioGraph(scenario.toDto().seed!!)
                runBlocking { graph.setupBattle(scenario) }
                graph to scenario
            }
        // Then
        graphs.forEach { (path, graphAndScenario) ->
            val (graph, scenario) = graphAndScenario
            assertThat(graph.battleApi.searchBattle()!!.currentPlayerTurn).isEqualTo("player-one")
            scenario.toDto().deployments.forEach { deployment ->
                val position = graph.battlefieldApi.searchPosition(deployment.battleUnitId)!!
                assertThat(position).isEqualTo(Battlefield.Dto.PositionDto(row = deployment.row, column = deployment.column))
                assertThat(graph.battlefieldApi.searchOccupant(row = deployment.row, column = deployment.column)).isEqualTo(deployment.battleUnitId)
            }
        }
    }

    @Test
    fun `should produce the same battlefield when the default scenario is set up instead of the default battle`() {
        // Given
        val defaultScenario = runBlocking { BattleScenarioLoader().load("scenarios/default.json") }
        val defaultBattleGraph = ScenarioGraph(42L)
        val scenarioGraph = ScenarioGraph(42L)
        // When
        runBlocking { defaultBattleGraph.setupBattle() }
        runBlocking { scenarioGraph.setupBattle(defaultScenario) }
        // Then
        val defaultBattlefield = defaultBattleGraph.battlefieldApi.searchBattlefield()!!
        val scenarioBattlefield = scenarioGraph.battlefieldApi.searchBattlefield()!!
        assertThat(scenarioBattlefield).isEqualTo(defaultBattlefield)
        assertThat(scenarioGraph.battleApi.searchBattle()!!.currentPlayerTurn).isEqualTo("player-one")
    }
}
