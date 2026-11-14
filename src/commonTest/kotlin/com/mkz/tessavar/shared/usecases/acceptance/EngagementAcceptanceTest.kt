package com.mkz.tessavar.shared.usecases.acceptance

import com.mkz.tessavar.ability.adapters.presentation.AbilityApi
import com.mkz.tessavar.ability.adapters.resources.ResourcesAbilityLoader
import com.mkz.tessavar.battle.adapters.presentation.BattleApi
import com.mkz.tessavar.battle.domain.BattleEvent
import com.mkz.tessavar.battleUnit.adapters.presentation.BattleUnitApi
import com.mkz.tessavar.battleUnit.adapters.resources.ResourcesBattleUnitLoader
import com.mkz.tessavar.battlefield.adapters.presentation.BattlefieldApi
import com.mkz.tessavar.battlefield.adapters.resources.ResourcesBattlefieldLoader
import com.mkz.tessavar.battlefield.adapters.resources.TomlBattlefieldDecoder
import com.mkz.tessavar.battlefield.adapters.resources.TomlBattlefieldEncoder
import com.mkz.tessavar.battlefield.domain.Battlefield.Dto.PositionDto
import com.mkz.tessavar.cpuBattleBrain.adapters.presentation.CpuBattleBrainApi
import com.mkz.tessavar.cpuWanderBrain.adapters.presentation.CpuWanderBrainApi
import com.mkz.tessavar.effect.adapters.presentation.EffectApi
import com.mkz.tessavar.effect.adapters.resources.ResourcesEffectLoader
import com.mkz.tessavar.effect.domain.Effect.Dto.EffectApplicationDto
import com.mkz.tessavar.effect.domain.Effect.Dto.EffectApplicationDto.SourceDto
import com.mkz.tessavar.effect.domain.Effect.Dto.RecipientDto
import com.mkz.tessavar.party.adapters.presentation.PartyApi
import com.mkz.tessavar.party.domain.Party.Dto.DirectionDto
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
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import kotlin.test.Test

class EngagementAcceptanceTest {
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
    private val playerTurns = mutableListOf<String>().also { turns -> eventBus.subscribe<BattleEvent.PlayerTurnStarted> { turns += it.playerId } }

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

    private fun engageRat() {
        partyApi.stepParty(DirectionDto.NORTH)
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
            battleUnitApi.walkBattleUnitInBattle(battleUnitId = "knight-1", walkToRow = 5, walkToColumn = 4)
            eventBus.dispatch()
            // When
            battleApi.finishPlayerTurn()
            eventBus.dispatch()
            // Then
            battleApi.searchBattle()!!.battleUnitIds shouldContain "rat-2"
            battleApi.searchBattle()!!.currentRound shouldBe 2
            battlefieldApi.searchPosition("rat-2") shouldNotBe PositionDto(row = 2, column = 6)
        }

    private fun defeatRat(ratId: String = "rat-1") {
        repeat(2) {
            battleUnitApi.applyEffect(
                EffectApplicationDto(
                    source = SourceDto.terrain("lava"),
                    recipient = RecipientDto.battleUnit(ratId),
                    effectId = "low-physical-damage",
                    destination = null,
                ),
            )
        }
        eventBus.dispatch()
    }

    @Test
    fun `should explore again straight away, without finishing the turn, when the last cpu battle unit in the battle is defeated`() =
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
    fun `should have a cpu battle unit within reach join the battle, which goes on, when the last cpu battle unit in the battle is defeated`() =
        scenarioTest(::setUp) {
            // Given
            engageRat()
            battleUnitApi.walkBattleUnitInBattle(battleUnitId = "knight-1", walkToRow = 5, walkToColumn = 4)
            eventBus.dispatch()
            // When
            defeatRat()
            // Then
            val battle = battleApi.searchBattle()!!
            battle.isFinished.shouldBeFalse()
            battle.battleUnitIds shouldBe setOf("rat-2", "knight-1", "knight-2")
            battle.currentRound shouldBe 1
            partyApi.searchParty()!!.isExploring.shouldBeFalse()
            battlefieldApi.searchPosition("rat-2") shouldBe PositionDto(row = 2, column = 6)
            playerTurns shouldBe listOf(cpuPlayerId, humanPlayerId)
        }

    @Test
    fun `should explore again when the last cpu battle unit of the battlefield is defeated`() =
        scenarioTest(::setUp) {
            // Given
            val finishedBattles = mutableListOf<BattleEvent.BattleFinished>().also { finished -> eventBus.subscribe<BattleEvent.BattleFinished> { finished += it } }
            engageRat()
            defeatRat("rat-2")
            battleApi.searchBattle()!!.isFinished.shouldBeFalse()
            // When
            defeatRat()
            // Then
            finishedBattles shouldBe listOf(BattleEvent.BattleFinished)
            battleApi.searchBattle()!!.isFinished.shouldBeTrue()
            partyApi.searchParty()!!.isExploring.shouldBeTrue()
        }

    @Test
    fun `should start a new battle when another cpu battle unit engages the party exploring again`() =
        scenarioTest(::setUp) {
            // Given
            engageRat()
            defeatRat()
            // When
            repeat(3) {
                partyApi.stepParty(DirectionDto.EAST)
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
