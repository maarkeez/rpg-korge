package com.mkz.rpg.shared.usecases.acceptance

import com.mkz.rpg.ability.adapters.presentation.AbilityApi
import com.mkz.rpg.battle.adapters.presentation.BattleApi
import com.mkz.rpg.battle.domain.BattleEvent
import com.mkz.rpg.battleUnit.adapters.presentation.BattleUnitApi
import com.mkz.rpg.battlefield.adapters.presentation.BattlefieldApi
import com.mkz.rpg.battlesetup.adapters.presentation.BattleSetupApi
import com.mkz.rpg.cpuBrain.adapters.presentation.CpuBrainApi
import com.mkz.rpg.effect.adapters.presentation.EffectApi
import com.mkz.rpg.player.adapters.presentation.PlayerApi
import com.mkz.rpg.shared.adapters.events.InMemoryEventBus
import com.mkz.rpg.shared.adapters.events.RecordingEventBus
import com.mkz.rpg.terrain.adapters.presentation.TerrainApi
import com.mkz.rpg.unit.adapters.presentation.UnitApi
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import kotlin.random.Random

class DeterministicBattleAcceptanceTest {
    private class Harness(
        val eventBus: RecordingEventBus,
        val terrainApi: TerrainApi,
        val battleApi: BattleApi,
        val battleSetupApi: BattleSetupApi,
    )

    private fun buildHarness(seed: Long): Harness {
        val random = Random(seed = seed)
        val eventBus = RecordingEventBus(InMemoryEventBus())
        val terrainApi = TerrainApi(eventBus)
        val unitApi = UnitApi(eventBus)
        val playerApi = PlayerApi(eventBus)
        val battlefieldApi = BattlefieldApi(terrainApi, eventBus)
        val effectApi = EffectApi(eventBus)
        val abilityApi = AbilityApi(effectApi, eventBus)
        val battleUnitApi = BattleUnitApi(effectApi, abilityApi, unitApi, playerApi, battlefieldApi, eventBus, random)
        val battleApi = BattleApi(eventBus, battleUnitApi)
        CpuBrainApi(unitApi, playerApi, battleUnitApi, battlefieldApi, terrainApi, effectApi, eventBus, random)
        val battleSetupApi = BattleSetupApi(eventBus)
        return Harness(eventBus, terrainApi, battleApi, battleSetupApi)
    }

    private fun setupDefaultBattle(harness: Harness) {
        runBlocking { harness.terrainApi.init() }
        harness.battleSetupApi.setupBattle()
        harness.eventBus.dispatch()
        harness.eventBus.clear()
    }

    private fun playCpuRounds(
        harness: Harness,
        maxRounds: Int,
    ) {
        repeat(maxRounds) {
            if (harness.eventBus.events.any { it is BattleEvent.PlayerVictory || it is BattleEvent.PlayerDefeated }) return
            harness.battleApi.finishPlayerTurn()
            harness.eventBus.dispatch()
        }
    }

    @Test
    fun `should produce identical event sequences when the same seed is used`() {
        // Given
        val firstHarness = buildHarness(seed = 42)
        setupDefaultBattle(firstHarness)
        val secondHarness = buildHarness(seed = 42)
        setupDefaultBattle(secondHarness)
        // When
        playCpuRounds(firstHarness, maxRounds = 4)
        playCpuRounds(secondHarness, maxRounds = 4)
        // Then
        assertThat(firstHarness.eventBus.events).isEqualTo(secondHarness.eventBus.events)
    }

    @Test
    fun `should include cpu decisions in the recorded events when playing cpu rounds`() {
        // Given
        val harness = buildHarness(seed = 42)
        setupDefaultBattle(harness)
        // When
        playCpuRounds(harness, maxRounds = 2)
        // Then
        assertThat(harness.eventBus.events).isNotEmpty
    }
}
