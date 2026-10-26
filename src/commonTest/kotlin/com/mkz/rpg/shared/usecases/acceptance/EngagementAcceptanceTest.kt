package com.mkz.rpg.shared.usecases.acceptance

import com.mkz.rpg.ability.adapters.presentation.AbilityApi
import com.mkz.rpg.battle.adapters.presentation.BattleApi
import com.mkz.rpg.battle.domain.BattleEvent
import com.mkz.rpg.battleUnit.adapters.presentation.BattleUnitApi
import com.mkz.rpg.battlefield.adapters.presentation.BattlefieldApi
import com.mkz.rpg.battlefield.domain.Battlefield.Dto.PositionDto
import com.mkz.rpg.cpuBattleBrain.adapters.presentation.CpuBattleBrainApi
import com.mkz.rpg.cpuWanderBrain.adapters.presentation.CpuWanderBrainApi
import com.mkz.rpg.effect.adapters.presentation.EffectApi
import com.mkz.rpg.effect.domain.Effect.Dto.EffectApplicationDto
import com.mkz.rpg.effect.domain.Effect.Dto.EffectApplicationDto.ApplicationSourceDto
import com.mkz.rpg.effect.domain.Effect.Dto.EffectTargetDto
import com.mkz.rpg.party.adapters.presentation.PartyApi
import com.mkz.rpg.party.domain.Party.Dto.DirectionDto
import com.mkz.rpg.player.adapters.presentation.PlayerApi
import com.mkz.rpg.player.usecases.commands.RequestPlayerCreation.PlayerType.CPU
import com.mkz.rpg.player.usecases.commands.RequestPlayerCreation.PlayerType.HUMAN
import com.mkz.rpg.shared.adapters.events.InMemoryEventBus
import com.mkz.rpg.shared.domain.FixedDoublesRandom
import com.mkz.rpg.shared.domain.subscribe
import com.mkz.rpg.terrain.adapters.presentation.TerrainApi
import com.mkz.rpg.unit.adapters.presentation.UnitApi
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import kotlin.test.Test

// Uses the units defined in the resources: two human knights, which walk 3 tiles in a battle, and two CPU rats.
class EngagementAcceptanceTest {
    private val eventBus = InMemoryEventBus()
    private val terrainApi = TerrainApi(eventBus)
    private val unitApi = UnitApi(eventBus)
    private val playerApi = PlayerApi(eventBus)
    private val battlefieldApi = BattlefieldApi(terrainApi, eventBus)
    private val effectApi = EffectApi(eventBus)
    private val abilityApi = AbilityApi(effectApi, eventBus)
    private val battleUnitApi = BattleUnitApi(effectApi, abilityApi, unitApi, playerApi, battlefieldApi, eventBus)
    private val battleApi = BattleApi(eventBus, playerApi)

    // The CPU battle units out of the battle rest when a round finishes, rather than wandering.
    private val cpuBattleBrainApi = CpuBattleBrainApi(unitApi, playerApi, battleUnitApi, battlefieldApi, terrainApi, effectApi, abilityApi, eventBus, FixedDoublesRandom(0.0))
    private val partyApi = PartyApi(playerApi, battleUnitApi, battlefieldApi, eventBus)
    private val cpuWanderBrainApi = CpuWanderBrainApi(playerApi, battleUnitApi, battlefieldApi, cpuBattleBrainApi, partyApi, eventBus)

    private val humanPlayerId = "human"
    private val cpuPlayerId = "cpu"
    private val playerTurns = mutableListOf<String>().also { turns -> eventBus.subscribe<BattleEvent.PlayerTurnStarted> { turns += it.playerId } }

    // The knights start in a line at (6,1) and (7,1). The rat-1, at (0,1), is 6 tiles away from the leader, and the rat-2,
    // at (2,6), 9 tiles away.
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
        battleUnitApi.deployBattleUnit("knight-1", "knight", humanPlayerId, deployAtRow = 6, deployAtColumn = 1)
        battleUnitApi.deployBattleUnit("knight-2", "knight", humanPlayerId, deployAtRow = 7, deployAtColumn = 1)
        battleUnitApi.deployBattleUnit("rat-1", "rat", cpuPlayerId, deployAtRow = 0, deployAtColumn = 1)
        battleUnitApi.deployBattleUnit("rat-2", "rat", cpuPlayerId, deployAtRow = 2, deployAtColumn = 6)
        partyApi.startExploration()
        eventBus.dispatch()
    }

    // The leader walks to (5,1), 5 tiles away from the rat-1, which engages the party.
    private fun engageRat() {
        partyApi.walkParty(DirectionDto.NORTH)
        eventBus.dispatch()
    }

    @Test
    fun `should battle with the engaging cpu battle unit and the whole party, the cpu playing first, when a cpu battle unit engages`() =
        scenarioTest(::setUp) {
            // When
            engageRat()
            // Then
            battleApi.searchBattle()!!.battleUnitIds shouldBe setOf("rat-1", "knight-1", "knight-2")
            playerTurns shouldBe listOf(cpuPlayerId, humanPlayerId)
        }

    @Test
    fun `should leave the cpu battle units out of the battle standing still when the cpu plays its turn`() =
        scenarioTest(::setUp) {
            // When
            engageRat()
            // Then
            battlefieldApi.searchPosition("rat-1") shouldNotBe PositionDto(row = 0, column = 1)
            battlefieldApi.searchPosition("rat-2") shouldBe PositionDto(row = 2, column = 6)
        }

    @Test
    fun `should have a cpu battle unit join the battle and play from the next round when the party comes within its reach`() =
        scenarioTest(::setUp) {
            // Given
            engageRat()
            // The leader walks to (5,4), 5 tiles away from the rat-2.
            battleUnitApi.moveBattleUnit(battleUnitId = "knight-1", moveToRow = 5, moveToColumn = 4)
            eventBus.dispatch()
            // When
            battleApi.finishPlayerTurn()
            eventBus.dispatch()
            // Then
            battleApi.searchBattle()!!.battleUnitIds shouldContain "rat-2"
            battleApi.searchBattle()!!.currentRound shouldBe 2
            battlefieldApi.searchPosition("rat-2") shouldNotBe PositionDto(row = 2, column = 6)
        }

    // The rat receives a low physical damage twice, losing its 20 health points, during the human turn.
    private fun defeatRat(ratId: String = "rat-1") {
        repeat(2) {
            battleUnitApi.applyEffect(
                EffectApplicationDto(
                    source = ApplicationSourceDto.terrain("lava"),
                    target = EffectTargetDto.Unit(ratId),
                    effectId = "low-physical-damage",
                ),
            )
        }
        eventBus.dispatch()
    }

    @Test
    fun `should explore again straight away, without finishing the turn, when the last cpu battle unit engaged is defeated`() =
        scenarioTest(::setUp) {
            // Given
            engageRat()
            // When
            defeatRat()
            // Then
            battleApi.searchBattle()!!.isFinished.shouldBeTrue()
            partyApi.searchParty()!!.isExploring.shouldBeTrue()
            playerTurns shouldBe listOf(cpuPlayerId, humanPlayerId)
            battlefieldApi.searchPosition("rat-2") shouldBe PositionDto(row = 2, column = 6)
        }

    @Test
    fun `should have a cpu battle unit within reach join the battle, which goes on, when the last cpu battle unit engaged is defeated`() =
        scenarioTest(::setUp) {
            // Given
            engageRat()
            // The leader walks to (5,4), 5 tiles away from the rat-2.
            battleUnitApi.moveBattleUnit(battleUnitId = "knight-1", moveToRow = 5, moveToColumn = 4)
            eventBus.dispatch()
            // When
            defeatRat()
            // Then
            val battle = battleApi.searchBattle()!!
            battle.isFinished.shouldBeFalse()
            battle.battleUnitIds shouldBe setOf("rat-2", "knight-1", "knight-2")
            battle.currentRound shouldBe 1
            partyApi.searchParty()!!.isExploring.shouldBeFalse()
            // The rat-2 plays from the next round.
            battlefieldApi.searchPosition("rat-2") shouldBe PositionDto(row = 2, column = 6)
            playerTurns shouldBe listOf(cpuPlayerId, humanPlayerId)
        }

    @Test
    fun `should only have the human player win, without exploring again, when the last cpu battle unit is defeated`() =
        scenarioTest(::setUp) {
            // Given
            val winners = mutableListOf<String>().also { winners -> eventBus.subscribe<BattleEvent.PlayerVictory> { winners += it.playerId } }
            val finishedBattles = mutableListOf<BattleEvent.BattleFinished>().also { finished -> eventBus.subscribe<BattleEvent.BattleFinished> { finished += it } }
            engageRat()
            // The rat-2, out of the battle, is defeated first, which leaves the battle engaged.
            defeatRat("rat-2")
            battleApi.searchBattle()!!.isFinished.shouldBeFalse()
            // When
            defeatRat()
            // Then
            winners shouldBe listOf(humanPlayerId)
            finishedBattles.shouldBeEmpty()
            partyApi.searchParty()!!.isExploring.shouldBeFalse()
        }

    @Test
    fun `should start a new battle when another cpu battle unit engages the party exploring again`() =
        scenarioTest(::setUp) {
            // Given
            engageRat()
            defeatRat()
            // When
            // The leader walks from (5,1) to (5,4), 5 tiles away from the rat-2.
            repeat(3) {
                partyApi.walkParty(DirectionDto.EAST)
                eventBus.dispatch()
            }
            // Then
            battlefieldApi.searchPosition("knight-1") shouldBe PositionDto(row = 5, column = 4)
            val battle = battleApi.searchBattle()!!
            battle.isFinished.shouldBeFalse()
            battle.battleUnitIds shouldBe setOf("rat-2", "knight-1", "knight-2")
            partyApi.searchParty()!!.isExploring.shouldBeFalse()
            playerTurns shouldBe listOf(cpuPlayerId, humanPlayerId, cpuPlayerId, humanPlayerId)
        }
}
