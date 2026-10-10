package com.mkz.rpg.shared.usecases.acceptance

import com.mkz.rpg.ability.adapters.presentation.AbilityApi
import com.mkz.rpg.battle.adapters.presentation.BattleApi
import com.mkz.rpg.battleUnit.adapters.presentation.BattleUnitApi
import com.mkz.rpg.battlefield.adapters.presentation.BattlefieldApi
import com.mkz.rpg.battlesetup.adapters.presentation.BattleSetupApi
import com.mkz.rpg.battlesetup.adapters.serialization.BattleScenarioLoader
import com.mkz.rpg.effect.adapters.presentation.EffectApi
import com.mkz.rpg.player.adapters.presentation.PlayerApi
import com.mkz.rpg.screen.battlefieldHud.adapters.storage.InMemoryBattlefieldHudRepository
import com.mkz.rpg.shared.adapters.events.InMemoryEventBus
import com.mkz.rpg.terrain.adapters.presentation.TerrainApi
import com.mkz.rpg.unit.adapters.presentation.UnitApi
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import kotlin.random.Random

class BattleStateFingerprintTest {
    private val eventBus = InMemoryEventBus()
    private val random = Random(42L)
    private val terrainApi = TerrainApi(eventBus)
    private val unitApi = UnitApi(eventBus)
    private val playerApi = PlayerApi(eventBus)
    private val battlefieldApi = BattlefieldApi(terrainApi, eventBus)
    private val effectApi = EffectApi(eventBus)
    private val abilityApi = AbilityApi(effectApi, eventBus)
    private val battleUnitApi = BattleUnitApi(effectApi, abilityApi, unitApi, playerApi, battlefieldApi, eventBus, random)
    private val battleApi = BattleApi(eventBus, battleUnitApi)
    private val battleSetupApi = BattleSetupApi(eventBus)
    private val battlefieldHudRepository = InMemoryBattlefieldHudRepository()

    private fun fingerprint() =
        BattleStateFingerprint.capture(
            playerIds = listOf("player-one", "player-two"),
            battleUnitApi = battleUnitApi,
            battlefieldApi = battlefieldApi,
            battleApi = battleApi,
            battlefieldHudRepository = battlefieldHudRepository,
        )

    @Test
    fun `should be equal when the battle was not touched between two captures`() {
        // Given
        val scenario = runBlocking { BattleScenarioLoader().load("scenarios/ui-showcase.json") }
        runBlocking { terrainApi.init() }
        battleSetupApi.setupBattle(scenario)
        eventBus.dispatch()
        // When
        val before = fingerprint()
        val after = fingerprint()
        // Then
        assertThat(before.battleUnits).hasSize(scenario.toDto().deployments.size)
        assertThat(after).isEqualTo(before)
    }

    @Test
    fun `should differ when a battle unit was moved between two captures`() {
        // Given
        val scenario = runBlocking { BattleScenarioLoader().load("scenarios/ui-showcase.json") }
        runBlocking { terrainApi.init() }
        battleSetupApi.setupBattle(scenario)
        eventBus.dispatch()
        val before = fingerprint()
        // When
        battleUnitApi.moveBattleUnit(battleUnitId = "player-1-unit-2", moveToRow = 9, moveToColumn = 6)
        eventBus.dispatch()
        // Then
        assertThat(fingerprint()).isNotEqualTo(before)
    }
}
