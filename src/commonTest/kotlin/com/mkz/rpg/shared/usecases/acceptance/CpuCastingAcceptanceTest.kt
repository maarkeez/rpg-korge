package com.mkz.rpg.shared.usecases.acceptance

import com.mkz.rpg.ability.adapters.presentation.AbilityApi
import com.mkz.rpg.battle.adapters.presentation.BattleApi
import com.mkz.rpg.battle.usecases.services.FakeBattleLoader
import com.mkz.rpg.battleUnit.adapters.presentation.BattleUnitApi
import com.mkz.rpg.battlefield.adapters.presentation.BattlefieldApi
import com.mkz.rpg.cpuBrain.adapters.presentation.CpuBrainApi
import com.mkz.rpg.effect.adapters.presentation.EffectApi
import com.mkz.rpg.player.adapters.presentation.PlayerApi
import com.mkz.rpg.player.usecases.commands.RequestPlayerCreation.PlayerType.CPU
import com.mkz.rpg.player.usecases.commands.RequestPlayerCreation.PlayerType.HUMAN
import com.mkz.rpg.shared.adapters.events.InMemoryEventBus
import com.mkz.rpg.terrain.adapters.presentation.TerrainApi
import com.mkz.rpg.unit.adapters.presentation.UnitApi
import io.kotest.matchers.shouldBe
import kotlin.test.Test

// Uses the effects, abilities and units defined in the resources: a CPU rat (20 HP, sword and heal) next to a human knight.
class CpuCastingAcceptanceTest {
    private val eventBus = InMemoryEventBus()
    private val terrainApi = TerrainApi(eventBus)
    private val unitApi = UnitApi(eventBus)
    private val playerApi = PlayerApi(eventBus)
    private val battlefieldApi = BattlefieldApi(terrainApi, eventBus)
    private val effectApi = EffectApi(eventBus)
    private val abilityApi = AbilityApi(effectApi, eventBus)
    private val battleUnitApi = BattleUnitApi(effectApi, abilityApi, unitApi, playerApi, battlefieldApi, eventBus)
    private val battleLoader = FakeBattleLoader()
    private val battleApi = BattleApi(eventBus, battleUnitApi, battleLoader)
    private val cpuBrainApi = CpuBrainApi(unitApi, playerApi, battleUnitApi, battlefieldApi, terrainApi, effectApi, abilityApi, eventBus)

    private val humanPlayerId = "human"
    private val cpuPlayerId = "cpu"
    private val knightBattleUnitId = "human-knight"
    private val ratBattleUnitId = "cpu-rat"

    private suspend fun setUp() {
        terrainApi.init()
        effectApi.init()
        abilityApi.init()
        unitApi.init()
        terrainApi.initialiseTerrains()
        effectApi.initialiseEffects()
        abilityApi.initialiseAbilities()
        unitApi.initialiseUnits()
        playerApi.requestPlayerCreation(humanPlayerId, "Human", HUMAN)
        playerApi.requestPlayerCreation(cpuPlayerId, "CPU", CPU)
        battlefieldApi.requestBattlefieldCreation(8, 8, List(8) { List(8) { "sand" } })
        battleUnitApi.deployBattleUnit(knightBattleUnitId, "knight", humanPlayerId, deployAtRow = 1, deployAtColumn = 1)
        battleUnitApi.deployBattleUnit(ratBattleUnitId, "rat", cpuPlayerId, deployAtRow = 1, deployAtColumn = 2)
        battleLoader.playerOrder = listOf(humanPlayerId, cpuPlayerId)
        battleApi.initialiseBattle()
        eventBus.dispatch()
    }

    @Test
    fun `should attack the adjacent knight when the cpu rat is healthy`() =
        scenarioTest(::setUp) {
            // Given
            require(battleUnitApi.searchBattleUnitById(ratBattleUnitId)!!.remainingHealthPoints == 20)
            battleApi.finishPlayerTurn()
            // When
            eventBus.dispatch()
            // Then
            battleUnitApi.searchBattleUnitById(knightBattleUnitId)!!.remainingHealthPoints shouldBe 90
        }
}
