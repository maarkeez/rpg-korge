package com.mkz.rpg.shared.usecases.acceptance

import com.mkz.rpg.ability.adapters.presentation.AbilityApi
import com.mkz.rpg.battle.adapters.presentation.BattleApi
import com.mkz.rpg.battle.adapters.presentation.startBattleWithEveryBattleUnit
import com.mkz.rpg.battleUnit.adapters.presentation.BattleUnitApi
import com.mkz.rpg.battlefield.adapters.presentation.BattlefieldApi
import com.mkz.rpg.battlefield.domain.Battlefield
import com.mkz.rpg.cpuBrain.adapters.presentation.CpuBrainApi
import com.mkz.rpg.effect.adapters.presentation.EffectApi
import com.mkz.rpg.player.adapters.presentation.PlayerApi
import com.mkz.rpg.player.usecases.commands.RequestPlayerCreation.PlayerType.CPU
import com.mkz.rpg.player.usecases.commands.RequestPlayerCreation.PlayerType.HUMAN
import com.mkz.rpg.shared.adapters.events.InMemoryEventBus
import com.mkz.rpg.terrain.adapters.presentation.TerrainApi
import com.mkz.rpg.unit.adapters.presentation.UnitApi
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import kotlin.test.Test

// Uses the effects, abilities and units defined in the resources: a CPU goat (bat, teleport and heal) and a human knight.
// The bat (sword dealing 10, walking 5 tiles per turn) is deployed on a vacant tile next to the goat.
class CpuDeployingAcceptanceTest {
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
    private val goatBattleUnitId = "cpu-goat"

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
        battleUnitApi.deployBattleUnit(goatBattleUnitId, "goat", cpuPlayerId, deployAtRow = 0, deployAtColumn = 0)
    }

    // Deploys the knight on the first row, as the goat, and starts the battle.
    private fun startBattleWithKnightAt(column: Int) {
        battleUnitApi.deployBattleUnit(knightBattleUnitId, "knight", humanPlayerId, deployAtRow = 0, deployAtColumn = column)
        battleApi.startBattleWithEveryBattleUnit(battleUnitApi, humanPlayerId, cpuPlayerId)
        eventBus.dispatch()
    }

    @Test
    fun `should deploy a bat on the tile nearest to the knight when the knight is within reach of the cpu goat`() =
        scenarioTest(::setUp) {
            // Given
            startBattleWithKnightAt(column = 3)
            battleApi.finishPlayerTurn()
            // When
            eventBus.dispatch()
            // Then
            val deployedBattleUnitIds = battleUnitApi.searchBattleUnitsByPlayerId(cpuPlayerId).map { it.id } - goatBattleUnitId
            deployedBattleUnitIds.size shouldBe 1
            val deployedBattleUnitId = deployedBattleUnitIds.single()
            battleUnitApi.searchBattleUnitById(deployedBattleUnitId)!!.unitId shouldBe "bat"
            battlefieldApi.searchOccupant(row = 0, column = 1) shouldBe deployedBattleUnitId
            // The bat joins the battle, so it plays the next CPU turns.
            battleApi.searchBattle()!!.battleUnitIds shouldContain deployedBattleUnitId
            battleApi.searchBattle()!!.currentPlayerTurn shouldBe humanPlayerId
        }

    @Test
    fun `should not deploy a bat when the knight is out of reach of the cpu goat`() =
        scenarioTest(::setUp) {
            // Given
            startBattleWithKnightAt(column = 7)
            battleApi.finishPlayerTurn()
            // When
            eventBus.dispatch()
            // Then
            battleUnitApi.searchBattleUnitsByPlayerId(cpuPlayerId).map { it.id }.shouldContainExactly(goatBattleUnitId)
            battlefieldApi.searchPosition(goatBattleUnitId) shouldBe Battlefield.Dto.PositionDto(row = 0, column = 0)
            // The goat finishes its turn, and leaves the battle once the round finishes, as the knight is out of its reach.
            battleApi.searchBattle()!!.isFinished.shouldBeTrue()
        }
}
