package com.mkz.rpg.shared.usecases.acceptance

import com.mkz.rpg.ability.adapters.presentation.AbilityApi
import com.mkz.rpg.battle.adapters.presentation.BattleApi
import com.mkz.rpg.battleUnit.adapters.presentation.BattleUnitApi
import com.mkz.rpg.battleUnit.domain.BattleUnitEvent
import com.mkz.rpg.battlefield.adapters.presentation.BattlefieldApi
import com.mkz.rpg.battlefield.domain.Battlefield.Dto.PositionDto
import com.mkz.rpg.cpuBrain.adapters.presentation.CpuBrainApi
import com.mkz.rpg.effect.adapters.presentation.EffectApi
import com.mkz.rpg.exploration.adapters.presentation.ExplorationApi
import com.mkz.rpg.player.adapters.presentation.PlayerApi
import com.mkz.rpg.player.usecases.commands.RequestPlayerCreation.PlayerType.CPU
import com.mkz.rpg.player.usecases.commands.RequestPlayerCreation.PlayerType.HUMAN
import com.mkz.rpg.shared.adapters.events.InMemoryEventBus
import com.mkz.rpg.shared.domain.FixedDoublesRandom
import com.mkz.rpg.shared.domain.subscribe
import com.mkz.rpg.terrain.adapters.presentation.TerrainApi
import com.mkz.rpg.unit.adapters.presentation.UnitApi
import com.mkz.rpg.wandering.adapters.presentation.WanderingApi
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import kotlin.test.Test

// Uses the units and terrains defined in the resources: two human knights, and a CPU rat, on sand.
class WanderingAcceptanceTest {
    private val humanPlayerId = "human"
    private val cpuPlayerId = "cpu"

    // A roll below 0.5 makes the rat rest. Otherwise, a roll of 0.9 picks the last steps it scores (north, east, south
    // and west), weighted by their utility.
    private inner class Game(
        vararg rolls: Double,
    ) {
        val eventBus = InMemoryEventBus()
        val terrainApi = TerrainApi(eventBus)
        val unitApi = UnitApi(eventBus)
        val playerApi = PlayerApi(eventBus)
        val battlefieldApi = BattlefieldApi(terrainApi, eventBus)
        val effectApi = EffectApi(eventBus)
        val abilityApi = AbilityApi(effectApi, eventBus)
        val battleUnitApi = BattleUnitApi(effectApi, abilityApi, unitApi, playerApi, battlefieldApi, eventBus)
        val battleApi = BattleApi(eventBus, playerApi, battleUnitApi)
        val cpuBrainApi = CpuBrainApi(unitApi, playerApi, battleUnitApi, battlefieldApi, terrainApi, effectApi, abilityApi, eventBus, FixedDoublesRandom(*rolls))
        val wanderingApi = WanderingApi(playerApi, battleUnitApi, battlefieldApi, cpuBrainApi, eventBus)
        val explorationApi = ExplorationApi(playerApi, battleUnitApi, battlefieldApi, eventBus)

        // The knights explore from the left side of an 8x8 battlefield, one above the other, and the rat is deployed at the
        // given tile, as the other rats.
        suspend fun start(
            ratRow: Int,
            ratColumn: Int,
            knightRow: Int,
            otherRats: Map<String, Pair<Int, Int>> = emptyMap(),
        ) {
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
            battleUnitApi.deployBattleUnit("knight-1", "knight", humanPlayerId, deployAtRow = knightRow, deployAtColumn = 0)
            battleUnitApi.deployBattleUnit("knight-2", "knight", humanPlayerId, deployAtRow = knightRow + 1, deployAtColumn = 0)
            battleUnitApi.deployBattleUnit("rat-1", "rat", cpuPlayerId, deployAtRow = ratRow, deployAtColumn = ratColumn)
            otherRats.forEach { (battleUnitId, tile) -> battleUnitApi.deployBattleUnit(battleUnitId, "rat", cpuPlayerId, deployAtRow = tile.first, deployAtColumn = tile.second) }
            explorationApi.startExploration()
            eventBus.dispatch()
        }

        // A second passes while exploring.
        fun waitASecond() {
            explorationApi.applyStandingTerrainEffects()
            eventBus.dispatch()
        }

        fun ratPosition() = battlefieldApi.searchPosition("rat-1")
    }

    @Test
    fun `should wander around its home, up to the edge of its territory, every second while exploring`() {
        // Given: the rat is deployed in the top right corner, at (0,7).
        val game = Game(0.9)
        scenarioTest({ game.start(ratRow = 0, ratColumn = 7, knightRow = 6) }) {
            // When
            val positions =
                List(5) {
                    game.waitASecond()
                    game.ratPosition()
                }
            // Then: it walks away from home three tiles, then back and forth along the edge of its territory.
            positions.shouldContainExactly(
                PositionDto(0, 6),
                PositionDto(0, 5),
                PositionDto(0, 4),
                PositionDto(0, 5),
                PositionDto(0, 4),
            )
            game.battleApi.searchBattle().shouldBeNull()
        }
    }

    @Test
    fun `should stand still while it rests`() {
        // Given
        val game = Game(0.0)
        scenarioTest({ game.start(ratRow = 0, ratColumn = 7, knightRow = 6) }) {
            // When
            repeat(5) { game.waitASecond() }
            // Then
            game.ratPosition() shouldBe PositionDto(0, 7)
        }
    }

    @Test
    fun `should engage the party when it wanders within its reach`() {
        // Given: the rat at (0,6) is 6 tiles away from the leader at (0,0), one more than its reach, and steps west.
        val game = Game(0.9)
        scenarioTest({ game.start(ratRow = 0, ratColumn = 6, knightRow = 0) }) {
            game.battleApi.searchBattle().shouldBeNull()
            // When
            game.waitASecond()
            // Then
            val battle = game.battleApi.searchBattle()!!
            battle.isFinished.shouldBeFalse()
            battle.battleUnitIds shouldContain "rat-1"
            game.explorationApi
                .searchExploration()!!
                .isExploring
                .shouldBeFalse()
        }
    }

    @Test
    fun `should walk back home without resting when it is left outside its territory`() {
        // Given: the rat is deployed at (0,7), and left 5 tiles away from home, at (5,7), e.g.: after chasing the party.
        val game = Game(0.0)
        scenarioTest({ game.start(ratRow = 0, ratColumn = 7, knightRow = 6) }) {
            game.battleUnitApi.walkBattleUnit(battleUnitId = "rat-1", walkToRow = 5, walkToColumn = 7)
            game.eventBus.dispatch()
            // When
            val positions =
                List(3) {
                    game.waitASecond()
                    game.ratPosition()
                }
            // Then: it heads home until it is back in its territory, where it rests.
            positions.shouldContainExactly(
                PositionDto(4, 7),
                PositionDto(3, 7),
                PositionDto(3, 7),
            )
        }
    }

    @Test
    fun `should join the battle when it wanders within reach of the party once a round finishes`() {
        // Given: rat-2 engages the knight-2 at (1,0) and battles. The rat at (0,6) stays out of the battle, 6 tiles away
        // from the leader at (0,0), one more than its reach, and steps west when the round finishes.
        val game = Game(0.9)
        scenarioTest({ game.start(ratRow = 0, ratColumn = 6, knightRow = 0, otherRats = mapOf("rat-2" to (3 to 0))) }) {
            game.battleApi.searchBattle()!!.battleUnitIds shouldNotContain "rat-1"
            val ratMoves = mutableListOf<PositionDto>()
            game.eventBus.subscribe<BattleUnitEvent.BattleUnitMoved> { event -> if (event.battleUnitId == "rat-1") ratMoves += PositionDto(event.toRow, event.toColumn) }
            // When
            game.battleApi.finishPlayerTurn()
            game.eventBus.dispatch()
            // Then: it joins the battle, so it approaches the party once the next round starts.
            ratMoves.first() shouldBe PositionDto(0, 5)
            game.battleApi.searchBattle()!!.battleUnitIds shouldContain "rat-1"
        }
    }
}
