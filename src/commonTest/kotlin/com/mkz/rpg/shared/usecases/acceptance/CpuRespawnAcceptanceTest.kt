package com.mkz.rpg.shared.usecases.acceptance

import com.mkz.rpg.ability.adapters.presentation.AbilityApi
import com.mkz.rpg.ability.adapters.resources.ResourcesAbilityLoader
import com.mkz.rpg.battle.adapters.presentation.BattleApi
import com.mkz.rpg.battleUnit.adapters.presentation.BattleUnitApi
import com.mkz.rpg.battleUnit.adapters.resources.ResourcesBattleUnitLoader
import com.mkz.rpg.battlefield.adapters.presentation.BattlefieldApi
import com.mkz.rpg.battlefield.adapters.resources.ResourcesBattlefieldLoader
import com.mkz.rpg.battlefield.adapters.resources.TomlBattlefieldDecoder
import com.mkz.rpg.battlefield.adapters.resources.TomlBattlefieldEncoder
import com.mkz.rpg.battlefield.domain.Battlefield.Dto.PositionDto
import com.mkz.rpg.cpuBattleBrain.adapters.presentation.CpuBattleBrainApi
import com.mkz.rpg.cpuWanderBrain.adapters.presentation.CpuWanderBrainApi
import com.mkz.rpg.cpuWanderBrain.domain.RESPAWN_STEPS
import com.mkz.rpg.effect.adapters.presentation.EffectApi
import com.mkz.rpg.effect.adapters.resources.ResourcesEffectLoader
import com.mkz.rpg.effect.domain.Effect.Dto.EffectApplicationDto
import com.mkz.rpg.effect.domain.Effect.Dto.EffectApplicationDto.SourceDto
import com.mkz.rpg.effect.domain.Effect.Dto.RecipientDto
import com.mkz.rpg.party.adapters.presentation.PartyApi
import com.mkz.rpg.party.domain.Party.Dto.DirectionDto
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
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class CpuRespawnAcceptanceTest {
    private val eventBus = InMemoryEventBus()
    private val terrainApi = TerrainApi(eventBus, ResourcesTerrainLoader())
    private val unitApi = UnitApi(eventBus, ResourcesUnitLoader())
    private val playerApi = PlayerApi(eventBus, ResourcesPlayerLoader())
    private val battlefieldApi = BattlefieldApi(terrainApi, eventBus, ResourcesBattlefieldLoader(), TomlBattlefieldEncoder(), TomlBattlefieldDecoder())
    private val effectApi = EffectApi(eventBus, ResourcesEffectLoader())
    private val abilityApi = AbilityApi(effectApi, eventBus, ResourcesAbilityLoader())
    private val battleUnitApi = BattleUnitApi(effectApi, abilityApi, unitApi, playerApi, battlefieldApi, eventBus, ResourcesBattleUnitLoader())
    private val battleApi = BattleApi(eventBus, playerApi)
    private val cpuBattleBrainApi = CpuBattleBrainApi(unitApi, playerApi, battleUnitApi, battlefieldApi, terrainApi, effectApi, abilityApi, eventBus, FixedDoublesRandom.cpuBattleUnitsAlwaysResting())
    private val partyApi = PartyApi(playerApi, battleUnitApi, battlefieldApi, eventBus)
    private val cpuWanderBrainApi = CpuWanderBrainApi(playerApi, battleUnitApi, battlefieldApi, cpuBattleBrainApi, partyApi, eventBus)

    private val ratHome = PositionDto(row = 1, column = 4)

    private suspend fun setUp() {
        terrainApi.init()
        effectApi.init()
        abilityApi.init()
        unitApi.init()
        terrainApi.initialiseTerrains()
        effectApi.initialiseEffects()
        abilityApi.initialiseAbilities()
        unitApi.initialiseUnits()
        playerApi.requestPlayerCreation("human", HUMAN)
        playerApi.requestPlayerCreation("cpu", CPU)
        val tiles = List(3) { List(12) { column -> if (column == 9) "void" else "sand" } }
        battlefieldApi.requestBattlefieldCreation(tiles.size, tiles.first().size, tiles)
        battleUnitApi.deployBattleUnit("rat-1", "rat", "cpu", deployAtRow = ratHome.row, deployAtColumn = ratHome.column)
        battleUnitApi.deployBattleUnit("rat-2", "rat", "cpu", deployAtRow = 1, deployAtColumn = 11)
        battleUnitApi.deployBattleUnit("knight-1", "knight", "human", deployAtRow = 1, deployAtColumn = 0)
        eventBus.dispatch()
        while (ratHealth() > 0) hitRat()
        partyApi.startExploration()
        eventBus.dispatch()
    }

    private fun ratHealth() = battleUnitApi.searchBattleUnitById("rat-1")!!.remainingHealth

    private fun hitRat() {
        battleUnitApi.applyEffect(
            EffectApplicationDto(
                source = SourceDto.terrain("lava"),
                recipient = RecipientDto.battleUnit("rat-1"),
                effectId = "low-physical-damage",
                destination = null,
            ),
        )
        eventBus.dispatch()
    }

    private fun walk(direction: DirectionDto) {
        partyApi.stepParty(direction)
        eventBus.dispatch()
    }

    private fun walkBackAndForth(steps: Int) = repeat(steps) { step -> walk(if (step % 2 == 0) DirectionDto.EAST else DirectionDto.WEST) }

    @Test
    fun `should not respawn the defeated cpu battle unit before the party has taken the respawn steps`() =
        scenarioTest(::setUp) {
            // When
            walkBackAndForth(RESPAWN_STEPS - 1)
            // Then
            ratHealth() shouldBe 0
            battlefieldApi.searchPosition("rat-1").shouldBeNull()
        }

    @Test
    fun `should respawn the defeated cpu battle unit on its home with full health once the party has taken the respawn steps`() =
        scenarioTest(::setUp) {
            // When
            walkBackAndForth(RESPAWN_STEPS)
            // Then
            val rat = battleUnitApi.searchBattleUnitById("rat-1")!!
            rat.remainingHealth shouldBe rat.maximumHealth
            battlefieldApi.searchPosition("rat-1") shouldBe ratHome
        }

    @Test
    fun `should wait while a battle unit of the party stands on its home`() =
        scenarioTest(::setUp) {
            // Given
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
