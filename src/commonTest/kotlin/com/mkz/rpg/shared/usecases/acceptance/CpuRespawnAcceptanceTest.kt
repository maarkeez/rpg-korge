package com.mkz.rpg.shared.usecases.acceptance

import com.mkz.rpg.ability.adapters.presentation.AbilityApi
import com.mkz.rpg.battle.adapters.presentation.BattleApi
import com.mkz.rpg.battleUnit.adapters.presentation.BattleUnitApi
import com.mkz.rpg.battlefield.adapters.presentation.BattlefieldApi
import com.mkz.rpg.battlefield.domain.Battlefield.Dto.PositionDto
import com.mkz.rpg.cpuBattleBrain.adapters.presentation.CpuBattleBrainApi
import com.mkz.rpg.cpuWanderBrain.adapters.presentation.CpuWanderBrainApi
import com.mkz.rpg.cpuWanderBrain.domain.RESPAWN_STEPS
import com.mkz.rpg.effect.adapters.presentation.EffectApi
import com.mkz.rpg.effect.domain.Effect.Dto.EffectApplicationDto
import com.mkz.rpg.effect.domain.Effect.Dto.EffectApplicationDto.ApplicationSourceDto
import com.mkz.rpg.effect.domain.Effect.Dto.EffectTargetDto
import com.mkz.rpg.exploration.adapters.presentation.ExplorationApi
import com.mkz.rpg.exploration.domain.Exploration.Dto.DirectionDto
import com.mkz.rpg.npc.adapters.presentation.NpcApi
import com.mkz.rpg.player.adapters.presentation.PlayerApi
import com.mkz.rpg.player.usecases.commands.RequestPlayerCreation.PlayerType.CPU
import com.mkz.rpg.player.usecases.commands.RequestPlayerCreation.PlayerType.HUMAN
import com.mkz.rpg.shared.adapters.events.InMemoryEventBus
import com.mkz.rpg.shared.domain.FixedDoublesRandom
import com.mkz.rpg.terrain.adapters.presentation.TerrainApi
import com.mkz.rpg.unit.adapters.presentation.UnitApi
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import kotlin.test.Test

// Uses the units and terrains defined in the resources: a human knight, and two CPU rats, on sand.
class CpuRespawnAcceptanceTest {
    private val eventBus = InMemoryEventBus()
    private val terrainApi = TerrainApi(eventBus)
    private val unitApi = UnitApi(eventBus)
    private val playerApi = PlayerApi(eventBus)
    private val battlefieldApi = BattlefieldApi(terrainApi, eventBus)
    private val effectApi = EffectApi(eventBus)
    private val abilityApi = AbilityApi(effectApi, eventBus)
    private val battleUnitApi = BattleUnitApi(effectApi, abilityApi, unitApi, playerApi, battlefieldApi, eventBus)
    private val battleApi = BattleApi(eventBus, playerApi, battleUnitApi)
    private val cpuBattleBrainApi = CpuBattleBrainApi(unitApi, playerApi, battleUnitApi, battlefieldApi, terrainApi, effectApi, abilityApi, eventBus, FixedDoublesRandom(0.0))
    private val cpuWanderBrainApi = CpuWanderBrainApi(playerApi, battleUnitApi, battlefieldApi, cpuBattleBrainApi, eventBus)
    private val explorationApi = ExplorationApi(playerApi, battleUnitApi, battlefieldApi, NpcApi(battlefieldApi, unitApi, eventBus), eventBus)

    private val ratHome = PositionDto(row = 1, column = 4)

    // The knight explores from (1,0), on a 3x12 battlefield of sand. The rat-1, at home on (1,4), is defeated before the
    // party explores. The rat-2 stands behind a wall of void, so it never engages the knight, and the CPU player is not
    // defeated with the rat-1.
    private suspend fun setUp() {
        terrainApi.init()
        effectApi.init()
        abilityApi.init()
        unitApi.init()
        terrainApi.initialiseTerrains()
        effectApi.initialiseEffects()
        abilityApi.initialiseAbilities()
        unitApi.initialiseUnits()
        playerApi.requestPlayerCreation("human", "Human", HUMAN)
        playerApi.requestPlayerCreation("cpu", "CPU", CPU)
        val tiles = List(3) { List(12) { column -> if (column == 9) "void" else "sand" } }
        battlefieldApi.requestBattlefieldCreation(tiles.size, tiles.first().size, tiles)
        battleUnitApi.deployBattleUnit("rat-1", "rat", "cpu", deployAtRow = ratHome.row, deployAtColumn = ratHome.column)
        battleUnitApi.deployBattleUnit("rat-2", "rat", "cpu", deployAtRow = 1, deployAtColumn = 11)
        battleUnitApi.deployBattleUnit("knight-1", "knight", "human", deployAtRow = 1, deployAtColumn = 0)
        eventBus.dispatch()
        while (ratHealth() > 0) hitRat()
        explorationApi.startExploration()
        eventBus.dispatch()
    }

    private fun ratHealth() = battleUnitApi.searchBattleUnitById("rat-1")!!.remainingHealthPoints

    // The sword hits the rat: 10 damage.
    private fun hitRat() {
        battleUnitApi.applyEffect(
            EffectApplicationDto(
                source = ApplicationSourceDto.terrain("lava"),
                target = EffectTargetDto.Unit("rat-1"),
                effectId = "low-physical-damage",
            ),
        )
        eventBus.dispatch()
    }

    private fun walk(direction: DirectionDto) {
        explorationApi.walkParty(direction)
        eventBus.dispatch()
    }

    // The knight walks back and forth between (1,0) and (1,1).
    private fun walkBackAndForth(steps: Int) = repeat(steps) { step -> walk(if (step % 2 == 0) DirectionDto.EAST else DirectionDto.WEST) }

    @Test
    fun `should not respawn the defeated cpu battle unit before the party has walked the respawn steps`() =
        scenarioTest(::setUp) {
            // When
            walkBackAndForth(RESPAWN_STEPS - 1)
            // Then
            ratHealth() shouldBe 0
            battlefieldApi.searchPosition("rat-1").shouldBeNull()
        }

    @Test
    fun `should respawn the defeated cpu battle unit on its home with full health once the party has walked the respawn steps`() =
        scenarioTest(::setUp) {
            // When
            walkBackAndForth(RESPAWN_STEPS)
            // Then
            val rat = battleUnitApi.searchBattleUnitById("rat-1")!!
            rat.remainingHealthPoints shouldBe rat.maximumHealthPoints
            battlefieldApi.searchPosition("rat-1") shouldBe ratHome
        }

    @Test
    fun `should wait while a battle unit of the party stands on its home`() =
        scenarioTest(::setUp) {
            // Given: the knight walks onto the home of the rat, on its 4th step, and onto it again on the last step.
            repeat(4) { walk(DirectionDto.EAST) }
            repeat(RESPAWN_STEPS - 4) { step -> walk(if (step % 2 == 0) DirectionDto.WEST else DirectionDto.EAST) }
            require(battlefieldApi.searchPosition("knight-1") == ratHome)
            require(ratHealth() == 0)
            // When
            walk(DirectionDto.WEST)
            // Then
            battlefieldApi.searchPosition("rat-1") shouldBe ratHome
        }
}
