package com.mkz.tessavar.shared.usecases.acceptance

import com.mkz.tessavar.ability.adapters.presentation.AbilityApi
import com.mkz.tessavar.ability.adapters.resources.ResourcesAbilityLoader
import com.mkz.tessavar.ability.domain.Ability
import com.mkz.tessavar.ability.domain.Ability.Dto.EffectTargetDto
import com.mkz.tessavar.ability.domain.AbilityMother
import com.mkz.tessavar.battle.adapters.presentation.BattleApi
import com.mkz.tessavar.battle.adapters.presentation.startBattleWithEveryBattleUnit
import com.mkz.tessavar.battleUnit.adapters.presentation.BattleUnitApi
import com.mkz.tessavar.battleUnit.adapters.resources.ResourcesBattleUnitLoader
import com.mkz.tessavar.battleUnit.domain.BattleUnitMother
import com.mkz.tessavar.battlefield.adapters.presentation.BattlefieldApi
import com.mkz.tessavar.battlefield.adapters.resources.ResourcesBattlefieldLoader
import com.mkz.tessavar.battlefield.adapters.resources.TomlBattlefieldDecoder
import com.mkz.tessavar.battlefield.adapters.resources.TomlBattlefieldEncoder
import com.mkz.tessavar.cpuBattleBrain.adapters.presentation.CpuBattleBrainApi
import com.mkz.tessavar.cpuWanderBrain.adapters.presentation.CpuWanderBrainApi
import com.mkz.tessavar.effect.adapters.presentation.EffectApi
import com.mkz.tessavar.effect.adapters.resources.ResourcesEffectLoader
import com.mkz.tessavar.effect.domain.EffectMother
import com.mkz.tessavar.party.adapters.presentation.PartyApi
import com.mkz.tessavar.player.adapters.presentation.PlayerApi
import com.mkz.tessavar.player.adapters.resources.ResourcesPlayerLoader
import com.mkz.tessavar.player.domain.PlayerMother
import com.mkz.tessavar.player.usecases.commands.RequestPlayerCreation.PlayerType.CPU
import com.mkz.tessavar.player.usecases.commands.RequestPlayerCreation.PlayerType.HUMAN
import com.mkz.tessavar.shared.adapters.events.InMemoryEventBus
import com.mkz.tessavar.shared.domain.FixedDoublesRandom
import com.mkz.tessavar.terrain.adapters.presentation.TerrainApi
import com.mkz.tessavar.terrain.adapters.resources.ResourcesTerrainLoader
import com.mkz.tessavar.unit.adapters.presentation.UnitApi
import com.mkz.tessavar.unit.adapters.resources.ResourcesUnitLoader
import com.mkz.tessavar.unit.domain.UnitMother
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class DeployBattleUnitEffectAcceptanceTest {
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

    private val humanPlayerId = PlayerMother.id()
    private val cpuPlayerId = PlayerMother.id()
    private val deployEffectId = EffectMother.effectId()
    private val deployAbilityId = AbilityMother.id()
    private val deployerUnitId = UnitMother.id()
    private val summonUnitId = UnitMother.id()
    private val casterBattleUnitId = BattleUnitMother.id()
    private val supportBattleUnitId = BattleUnitMother.id()

    private suspend fun setUp() {
        playerApi.requestPlayerCreation(humanPlayerId, HUMAN)
        playerApi.requestPlayerCreation(cpuPlayerId, CPU)
        terrainApi.init()
        terrainApi.initialiseTerrains()
        battlefieldApi.requestBattlefieldCreation(8, 8, List(8) { List(8) { "sand" } })

        val deployEffect = EffectMother.summonEffect(id = deployEffectId, unitId = summonUnitId)
        effectApi.requestEffectCreation(deployEffect.toDto())

        val deployAbility =
            AbilityMother.ability(
                id = deployAbilityId,
                manaCost = 0,
                cooldown = 0,
                abilityEffects = listOf(AbilityMother.abilityEffect(effectId = deployEffectId, target = EffectTargetDto.Type.TARGETED_TILE)),
                targeting = Ability.Dto.TargetingDto.VACANT_TILE_ADJACENT_TO_ANOTHER_BATTLE_UNIT,
                summonDuration = 5,
            )
        abilityApi.requestAbilityCreation(deployAbility.toDto())

        val deployerUnit =
            UnitMother.unit(
                id = deployerUnitId,
                baseMaximumHealth = 100,
                baseMaximumMana = 30,
                startingAbilities = listOf(deployAbilityId),
                maximumSteps = 3,
            )
        val summonUnit =
            UnitMother.unit(
                id = summonUnitId,
                baseMaximumHealth = 20,
                baseMaximumMana = 10,
                startingAbilities = emptyList(),
                maximumSteps = 3,
            )
        unitApi.requestUnitCreation(deployerUnit.toDto())
        unitApi.requestUnitCreation(summonUnit.toDto())

        battleUnitApi.deployBattleUnit(
            battleUnitId = casterBattleUnitId,
            unitId = deployerUnitId,
            playerId = humanPlayerId,
            deployAtRow = 2,
            deployAtColumn = 2,
        )
        battleUnitApi.deployBattleUnit(
            battleUnitId = supportBattleUnitId,
            unitId = deployerUnitId,
            playerId = humanPlayerId,
            deployAtRow = 2,
            deployAtColumn = 3,
        )
        battleApi.startBattleWithEveryBattleUnit(battleUnitApi, humanPlayerId, cpuPlayerId)
        eventBus.dispatch()
    }

    @Test
    fun `should deploy a new battle unit assigned to the caster player when the deploy battle unit effect is applied`() =
        scenarioTest(::setUp) {
            // Given
            require(battlefieldApi.searchOccupant(row = 2, column = 2) == casterBattleUnitId)
            require(battlefieldApi.searchOccupant(row = 2, column = 3) == supportBattleUnitId)
            require(battleApi.searchBattle()!!.currentPlayerId == humanPlayerId)
            val castGroup = battleUnitApi.whereCanCast(casterBattleUnitId, deployAbilityId).first()
            val target = castGroup.positions.first()
            val humanUnitsBefore = battleUnitApi.searchBattleUnitsByPlayerId(humanPlayerId).size
            battleUnitApi.castAbility(
                battleUnitId = casterBattleUnitId,
                abilityId = deployAbilityId,
                castGroup = castGroup,
            )
            // When
            eventBus.dispatch()
            // Then
            val humanUnitsAfter = battleUnitApi.searchBattleUnitsByPlayerId(humanPlayerId)
            humanUnitsAfter.size shouldBe humanUnitsBefore + 1
            val deployedOccupantId = battlefieldApi.searchOccupant(row = target.row, column = target.column)
            deployedOccupantId.shouldNotBeNull()
            val deployed = battleUnitApi.searchBattleUnitById(deployedOccupantId!!)!!
            deployed.playerId shouldBe humanPlayerId
            deployed.unitId shouldBe summonUnitId
        }
}
