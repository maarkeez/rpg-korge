package com.mkz.rpg.shared.usecases.acceptance

import com.mkz.rpg.ability.adapters.presentation.AbilityApi
import com.mkz.rpg.ability.adapters.resources.ResourcesAbilityLoader
import com.mkz.rpg.battle.adapters.presentation.BattleApi
import com.mkz.rpg.battle.adapters.presentation.startBattleWithEveryBattleUnit
import com.mkz.rpg.battleUnit.adapters.presentation.BattleUnitApi
import com.mkz.rpg.battleUnit.adapters.resources.ResourcesBattleUnitLoader
import com.mkz.rpg.battlefield.adapters.presentation.BattlefieldApi
import com.mkz.rpg.battlefield.adapters.resources.ResourcesBattlefieldLoader
import com.mkz.rpg.battlefield.adapters.resources.TomlBattlefieldDecoder
import com.mkz.rpg.battlefield.adapters.resources.TomlBattlefieldEncoder
import com.mkz.rpg.cpuBattleBrain.adapters.presentation.CpuBattleBrainApi
import com.mkz.rpg.cpuWanderBrain.adapters.presentation.CpuWanderBrainApi
import com.mkz.rpg.effect.adapters.presentation.EffectApi
import com.mkz.rpg.effect.adapters.resources.ResourcesEffectLoader
import com.mkz.rpg.party.adapters.presentation.PartyApi
import com.mkz.rpg.player.adapters.presentation.PlayerApi
import com.mkz.rpg.player.adapters.resources.ResourcesPlayerLoader
import com.mkz.rpg.player.usecases.commands.RequestPlayerCreation.PlayerType.CPU
import com.mkz.rpg.player.usecases.commands.RequestPlayerCreation.PlayerType.HUMAN
import com.mkz.rpg.shared.adapters.events.InMemoryEventBus
import com.mkz.rpg.shared.domain.FixedDoublesRandom
import com.mkz.rpg.terrain.adapters.presentation.TerrainApi
import com.mkz.rpg.terrain.adapters.resources.ResourcesTerrainLoader
import com.mkz.rpg.unit.adapters.presentation.UnitApi
import com.mkz.rpg.unit.adapters.resources.ResourcesUnitLoader
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import kotlin.math.abs
import kotlin.test.Test

// Uses the effects, abilities and units defined in the resources: CPU rats (20 HP, sword and heal) and a human knight.
class CpuTurnAcceptanceTest {
    private val eventBus = InMemoryEventBus()
    private val terrainApi = TerrainApi(eventBus, ResourcesTerrainLoader())
    private val unitApi = UnitApi(eventBus, ResourcesUnitLoader())
    private val playerApi = PlayerApi(eventBus, ResourcesPlayerLoader())
    private val battlefieldApi = BattlefieldApi(terrainApi, eventBus, ResourcesBattlefieldLoader(), TomlBattlefieldEncoder(), TomlBattlefieldDecoder())
    private val effectApi = EffectApi(eventBus, ResourcesEffectLoader())
    private val abilityApi = AbilityApi(effectApi, eventBus, ResourcesAbilityLoader())
    private val battleUnitApi = BattleUnitApi(effectApi, abilityApi, unitApi, playerApi, battlefieldApi, eventBus, ResourcesBattleUnitLoader())
    private val battleApi = BattleApi(eventBus, playerApi)

    // The CPU battle units out of the battle rest when a round finishes, rather than wandering.
    private val cpuBattleBrainApi = CpuBattleBrainApi(unitApi, playerApi, battleUnitApi, battlefieldApi, terrainApi, effectApi, abilityApi, eventBus, FixedDoublesRandom(0.0))
    private val partyApi = PartyApi(playerApi, battleUnitApi, battlefieldApi, eventBus)
    private val cpuWanderBrainApi = CpuWanderBrainApi(playerApi, battleUnitApi, battlefieldApi, cpuBattleBrainApi, partyApi, eventBus)

    private val humanPlayerId = "human"
    private val cpuPlayerId = "cpu"
    private val knightBattleUnitId = "human-knight"

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
        battleApi.startBattleWithEveryBattleUnit(battleUnitApi, humanPlayerId, cpuPlayerId)
        eventBus.dispatch()
    }

    private fun distanceToKnight(battleUnitId: String): Int {
        val position = battlefieldApi.searchPosition(battleUnitId)!!
        return abs(position.row - 3) + abs(position.column - 3)
    }

    @Test
    fun `should move next to the knight and attack it in the same turn when the cpu rat is two tiles away`() =
        scenarioTest(::setUp) {
            // Given
            deployRat(battleUnitId = "cpu-rat", row = 3, column = 5)
            startBattle()
            require(distanceToKnight("cpu-rat") == 2)
            battleApi.finishPlayerTurn()
            // When
            eventBus.dispatch()
            // Then
            distanceToKnight("cpu-rat") shouldBe 1
            battleUnitApi.searchBattleUnitById(knightBattleUnitId)!!.remainingHealthPoints shouldBe 90
        }

    @Test
    fun `should move each cpu rat to a different tile next to the knight and attack with both when they are two tiles away`() =
        scenarioTest(::setUp) {
            // Given
            // On both sides of the knight, so the first rat to move does not stand in the way of the other.
            deployRat(battleUnitId = "cpu-rat-1", row = 3, column = 5)
            deployRat(battleUnitId = "cpu-rat-2", row = 3, column = 1)
            startBattle()
            battleApi.finishPlayerTurn()
            // When
            eventBus.dispatch()
            // Then
            battlefieldApi.searchPosition("cpu-rat-1") shouldNotBe battlefieldApi.searchPosition("cpu-rat-2")
            distanceToKnight("cpu-rat-1") shouldBe 1
            distanceToKnight("cpu-rat-2") shouldBe 1
            battleUnitApi.searchBattleUnitById(knightBattleUnitId)!!.remainingHealthPoints shouldBe 80
        }

    @Test
    fun `should give the turn back to the human player when the cpu turn ends`() =
        scenarioTest(::setUp) {
            // Given
            deployRat(battleUnitId = "cpu-rat", row = 3, column = 5)
            startBattle()
            battleApi.finishPlayerTurn()
            // When
            eventBus.dispatch()
            // Then
            val battle = battleApi.searchBattle()!!
            battle.currentPlayerTurn shouldBe humanPlayerId
            battle.currentRound shouldBe 2
        }
}
