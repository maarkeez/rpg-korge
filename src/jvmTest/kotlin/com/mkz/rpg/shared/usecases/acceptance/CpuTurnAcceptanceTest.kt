package com.mkz.rpg.shared.usecases.acceptance

import com.mkz.rpg.ability.adapters.presentation.AbilityApi
import com.mkz.rpg.battle.adapters.presentation.BattleApi
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
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.math.abs

// Uses the effects, abilities and units defined in the resources: CPU rats (20 HP, sword and heal) and a human knight.
class CpuTurnAcceptanceTest {
    private val eventBus = InMemoryEventBus()
    private val terrainApi = TerrainApi(eventBus)
    private val unitApi = UnitApi(eventBus)
    private val playerApi = PlayerApi(eventBus)
    private val battlefieldApi = BattlefieldApi(terrainApi, eventBus)
    private val effectApi = EffectApi(eventBus)
    private val abilityApi = AbilityApi(effectApi, eventBus)
    private val battleUnitApi = BattleUnitApi(effectApi, abilityApi, unitApi, playerApi, battlefieldApi, eventBus)
    private val battleApi = BattleApi(eventBus, battleUnitApi)
    private val cpuBrainApi = CpuBrainApi(unitApi, playerApi, battleUnitApi, battlefieldApi, terrainApi, effectApi, abilityApi, eventBus)

    private val humanPlayerId = "human"
    private val cpuPlayerId = "cpu"
    private val knightBattleUnitId = "human-knight"

    @BeforeEach
    fun setup() {
        runBlocking {
            terrainApi.init()
            effectApi.init()
            abilityApi.init()
            unitApi.init()
        }
        terrainApi.initialiseTerrains()
        effectApi.initialiseEffects()
        abilityApi.initialiseAbilities()
        unitApi.initialiseUnits()
        playerApi.requestPlayerCreation(humanPlayerId, "Human", HUMAN)
        playerApi.requestPlayerCreation(cpuPlayerId, "CPU", CPU)
        battlefieldApi.requestBattlefieldCreation(8, 8, List(8) { List(8) { "sand" } })
        battleUnitApi.deployBattleUnit(knightBattleUnitId, "knight", humanPlayerId, deployAtRow = 3, deployAtColumn = 3)
    }

    private fun deployRat(
        battleUnitId: String,
        row: Int,
        column: Int,
    ) {
        battleUnitApi.deployBattleUnit(battleUnitId, "rat", cpuPlayerId, deployAtRow = row, deployAtColumn = column)
    }

    private fun startBattle() {
        battleApi.startFirstRound(listOf(humanPlayerId, cpuPlayerId))
        eventBus.dispatch()
    }

    private fun distanceToKnight(battleUnitId: String): Int {
        val position = battlefieldApi.searchPosition(battleUnitId)!!
        return abs(position.row - 3) + abs(position.column - 3)
    }

    @Test
    fun `should move next to the knight and attack it in the same turn when the cpu rat is two tiles away`() {
        // Given
        deployRat(battleUnitId = "cpu-rat", row = 3, column = 5)
        startBattle()
        require(distanceToKnight("cpu-rat") == 2)
        battleApi.finishPlayerTurn()
        // When
        eventBus.dispatch()
        // Then
        assertThat(distanceToKnight("cpu-rat")).isEqualTo(1)
        assertThat(battleUnitApi.searchBattleUnitById(knightBattleUnitId)!!.remainingHealthPoints).isEqualTo(90)
    }

    @Test
    fun `should move each cpu rat to a different tile next to the knight and attack with both when they are two tiles away`() {
        // Given
        deployRat(battleUnitId = "cpu-rat-1", row = 3, column = 5)
        deployRat(battleUnitId = "cpu-rat-2", row = 1, column = 3)
        startBattle()
        battleApi.finishPlayerTurn()
        // When
        eventBus.dispatch()
        // Then
        assertThat(battlefieldApi.searchPosition("cpu-rat-1")).isNotEqualTo(battlefieldApi.searchPosition("cpu-rat-2"))
        assertThat(distanceToKnight("cpu-rat-1")).isEqualTo(1)
        assertThat(distanceToKnight("cpu-rat-2")).isEqualTo(1)
        assertThat(battleUnitApi.searchBattleUnitById(knightBattleUnitId)!!.remainingHealthPoints).isEqualTo(80)
    }

    @Test
    fun `should give the turn back to the human player when the cpu turn ends`() {
        // Given
        deployRat(battleUnitId = "cpu-rat", row = 3, column = 5)
        startBattle()
        battleApi.finishPlayerTurn()
        // When
        eventBus.dispatch()
        // Then
        val battle = battleApi.searchBattle()!!
        assertThat(battle.currentPlayerTurn).isEqualTo(humanPlayerId)
        assertThat(battle.currentRound).isEqualTo(2)
    }
}
