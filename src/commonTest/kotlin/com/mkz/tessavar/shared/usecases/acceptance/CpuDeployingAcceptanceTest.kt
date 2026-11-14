package com.mkz.tessavar.shared.usecases.acceptance

import com.mkz.tessavar.ability.adapters.presentation.AbilityApi
import com.mkz.tessavar.ability.adapters.resources.ResourcesAbilityLoader
import com.mkz.tessavar.battle.adapters.presentation.BattleApi
import com.mkz.tessavar.battle.adapters.presentation.startBattleWithEveryBattleUnit
import com.mkz.tessavar.battleUnit.adapters.presentation.BattleUnitApi
import com.mkz.tessavar.battleUnit.adapters.resources.ResourcesBattleUnitLoader
import com.mkz.tessavar.battleUnit.domain.BattleUnitEvent
import com.mkz.tessavar.battlefield.adapters.presentation.BattlefieldApi
import com.mkz.tessavar.battlefield.adapters.resources.ResourcesBattlefieldLoader
import com.mkz.tessavar.battlefield.adapters.resources.TomlBattlefieldDecoder
import com.mkz.tessavar.battlefield.adapters.resources.TomlBattlefieldEncoder
import com.mkz.tessavar.battlefield.domain.Battlefield
import com.mkz.tessavar.cpuBattleBrain.adapters.presentation.CpuBattleBrainApi
import com.mkz.tessavar.cpuWanderBrain.adapters.presentation.CpuWanderBrainApi
import com.mkz.tessavar.effect.adapters.presentation.EffectApi
import com.mkz.tessavar.effect.adapters.resources.ResourcesEffectLoader
import com.mkz.tessavar.party.adapters.presentation.PartyApi
import com.mkz.tessavar.player.adapters.presentation.PlayerApi
import com.mkz.tessavar.player.adapters.resources.ResourcesPlayerLoader
import com.mkz.tessavar.player.usecases.commands.RequestPlayerCreation.PlayerType.CPU
import com.mkz.tessavar.player.usecases.commands.RequestPlayerCreation.PlayerType.HUMAN
import com.mkz.tessavar.shared.adapters.events.InMemoryEventBus
import com.mkz.tessavar.shared.domain.FixedDoublesRandom
import com.mkz.tessavar.shared.domain.subscribe
import com.mkz.tessavar.terrain.adapters.presentation.TerrainApi
import com.mkz.tessavar.terrain.adapters.resources.ResourcesTerrainLoader
import com.mkz.tessavar.unit.adapters.presentation.UnitApi
import com.mkz.tessavar.unit.adapters.resources.ResourcesUnitLoader
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class CpuDeployingAcceptanceTest {
    private val eventBus = InMemoryEventBus()
    private val terrainApi = TerrainApi(eventBus, ResourcesTerrainLoader())
    private val unitApi = UnitApi(eventBus, ResourcesUnitLoader())
    private val playerApi = PlayerApi(eventBus, ResourcesPlayerLoader())
    private val battlefieldApi = BattlefieldApi(terrainApi, eventBus, ResourcesBattlefieldLoader(), TomlBattlefieldEncoder(), TomlBattlefieldDecoder())
    private val effectApi = EffectApi(eventBus, ResourcesEffectLoader())
    private val abilityApi = AbilityApi(effectApi, eventBus, ResourcesAbilityLoader())
    private val battleUnitApi = BattleUnitApi(effectApi, abilityApi, unitApi, playerApi, battlefieldApi, eventBus, ResourcesBattleUnitLoader())
    private val battleApi = BattleApi(eventBus)

    private val cpuBattleBrainApi = CpuBattleBrainApi(unitApi, playerApi, battleUnitApi, battlefieldApi, terrainApi, effectApi, abilityApi, eventBus, FixedDoublesRandom.cpuBattleUnitsAlwaysResting())
    private val partyApi = PartyApi(playerApi, battleUnitApi, battlefieldApi, eventBus)
    private val cpuWanderBrainApi = CpuWanderBrainApi(playerApi, battleUnitApi, battlefieldApi, cpuBattleBrainApi, partyApi, eventBus)

    private val humanPlayerId = "human"
    private val cpuPlayerId = "cpu"
    private val knightBattleUnitId = "human-knight"
    private val goatBattleUnitId = "cpu-goat"
    private val deployments = mutableListOf<BattleUnitEvent.BattleUnitDeployed>().also { deployments -> eventBus.subscribe<BattleUnitEvent.BattleUnitDeployed> { deployments += it } }

    private suspend fun setUp() {
        terrainApi.init()
        effectApi.init()
        abilityApi.init()
        unitApi.init()
        terrainApi.initialiseTerrains()
        effectApi.initialiseEffects()
        abilityApi.initialiseAbilities()
        unitApi.initialiseUnits()
        playerApi.requestPlayerCreation(humanPlayerId, HUMAN)
        playerApi.requestPlayerCreation(cpuPlayerId, CPU)
        battlefieldApi.requestBattlefieldCreation(8, 8, List(8) { List(8) { "sand" } })
        battleUnitApi.deployBattleUnit(goatBattleUnitId, "goat", cpuPlayerId, deployAtRow = 0, deployAtColumn = 0)
    }

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
            deployments.last() shouldBe BattleUnitEvent.BattleUnitDeployed(playerId = cpuPlayerId, battleUnitId = deployedBattleUnitId, row = 0, column = 1)
            battleApi.searchBattle()!!.battleUnitIds shouldContain deployedBattleUnitId
            battleApi.searchBattle()!!.currentPlayerId shouldBe humanPlayerId
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
            battleApi.searchBattle()!!.isFinished.shouldBeTrue()
        }

    @Test
    fun `should have the bat attack the knight in the turn the cpu goat summons it`() =
        scenarioTest(::setUp) {
            // Given
            startBattleWithKnightAt(column = 3)
            battleApi.finishPlayerTurn()
            // When
            eventBus.dispatch()
            // Then
            val batId = (battleUnitApi.searchBattleUnitsByPlayerId(cpuPlayerId).map { it.id } - goatBattleUnitId).single()
            battlefieldApi.searchPosition(batId) shouldBe Battlefield.Dto.PositionDto(row = 0, column = 2)
            battleUnitApi.searchBattleUnitById(knightBattleUnitId)!!.remainingHealth shouldBe 90
            battleApi.searchBattle()!!.currentPlayerId shouldBe humanPlayerId
        }
}
