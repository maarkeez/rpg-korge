package com.mkz.rpg.battlesetup.adapters.serialization

import com.mkz.rpg.battlesetup.domain.Battlesetup
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class BattleScenarioLoaderTest {
    private val starterScenarioPaths =
        listOf(
            "scenarios/default.json",
            "scenarios/chain-showcase.json",
            "scenarios/terrain-mix.json",
            "scenarios/ui-showcase.json",
        )

    @Test
    fun `should load the default scenario when the default scenario file is loaded`() {
        // When
        val scenario = runBlocking { BattleScenarioLoader().load("scenarios/default.json") }
        // Then
        val dto = scenario.toDto()
        assertThat(dto.seed).isEqualTo(42L)
        assertThat(dto.battlefield.rows).isEqualTo(16)
        assertThat(dto.battlefield.columns).isEqualTo(16)
        assertThat(dto.battlefield.tiles).hasSize(16)
        assertThat(dto.battlefield.tiles[5][3]).isEqualTo("lava")
        assertThat(dto.battlefield.tiles[7][1]).isEqualTo("water")
        assertThat(dto.battlefield.tiles[2][7]).isEqualTo("void")
        assertThat(dto.deployments).hasSize(4)
        assertThat(dto.deployments.map { it.battleUnitId })
            .containsExactly("player-2-unit-1", "player-2-unit-2", "player-1-unit-1", "player-1-unit-2")
        val knightDeployment = dto.deployments.single { it.battleUnitId == "player-1-unit-1" }
        assertThat(knightDeployment.unitId).isEqualTo("knight")
        assertThat(knightDeployment.playerId).isEqualTo("player-one")
        assertThat(knightDeployment.row).isEqualTo(6)
        assertThat(knightDeployment.column).isEqualTo(6)
    }

    @Test
    fun `should load consistent battlefields when every starter scenario file is loaded`() {
        // When
        val scenarios =
            runBlocking {
                val loadedScenarios = LinkedHashMap<String, Battlesetup>()
                for (path in starterScenarioPaths) {
                    loadedScenarios[path] = BattleScenarioLoader().load(path)
                }
                loadedScenarios
            }
        // Then
        scenarios.forEach { (path, scenario) ->
            val dto = scenario.toDto()
            assertThat(dto.seed).isNotNull
            assertThat(dto.battlefield.tiles).hasSize(dto.battlefield.rows)
            dto.battlefield.tiles.forEach { row ->
                assertThat(row).hasSize(dto.battlefield.columns)
            }
            dto.deployments.forEach { deployment ->
                assertThat(deployment.row).isBetween(0, dto.battlefield.rows - 1)
                assertThat(deployment.column).isBetween(0, dto.battlefield.columns - 1)
            }
        }
    }

    @Test
    fun `should produce an equal scenario json when a scenario json is encoded and decoded`() {
        // Given
        val original =
            BattleScenarioJson(
                seed = 7L,
                battlefield =
                    BattleScenarioJson.BattlefieldJson(
                        rows = 2,
                        columns = 2,
                        tiles =
                            listOf(
                                listOf("sand", "lava"),
                                listOf("water", "grass"),
                            ),
                    ),
                deployments =
                    listOf(
                        BattleScenarioJson.DeploymentJson(battleUnitId = "unit-1", unitId = "rat", playerId = "player-two", row = 0, column = 1),
                    ),
            )
        // When
        val decoded = Json.decodeFromString<BattleScenarioJson>(Json.encodeToString(original))
        // Then
        assertThat(decoded).isEqualTo(original)
    }
}
