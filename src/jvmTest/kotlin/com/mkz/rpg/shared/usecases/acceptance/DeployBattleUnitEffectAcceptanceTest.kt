package com.mkz.rpg.shared.usecases.acceptance

import com.mkz.rpg.ability.adapters.presentation.AbilityApi
import com.mkz.rpg.ability.domain.Ability
import com.mkz.rpg.ability.domain.Ability.Dto.TargetExpressionDto
import com.mkz.rpg.ability.domain.AbilityMother
import com.mkz.rpg.battle.adapters.presentation.BattleApi
import com.mkz.rpg.battleUnit.adapters.presentation.BattleUnitApi
import com.mkz.rpg.battleUnit.domain.BattleUnitMother
import com.mkz.rpg.battlefield.adapters.presentation.BattlefieldApi
import com.mkz.rpg.cpuBrain.adapters.presentation.CpuBrainApi
import com.mkz.rpg.effect.adapters.presentation.EffectApi
import com.mkz.rpg.effect.domain.EffectMother
import com.mkz.rpg.player.adapters.presentation.PlayerApi
import com.mkz.rpg.player.domain.PlayerMother
import com.mkz.rpg.player.usecases.commands.RequestPlayerCreation.PlayerType.CPU
import com.mkz.rpg.player.usecases.commands.RequestPlayerCreation.PlayerType.HUMAN
import com.mkz.rpg.shared.adapters.events.InMemoryEventBus
import com.mkz.rpg.unit.adapters.presentation.UnitApi
import com.mkz.rpg.unit.domain.UnitMother
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class DeployBattleUnitEffectAcceptanceTest {
    private val eventBus = InMemoryEventBus()
    private val unitApi = UnitApi(eventBus)
    private val playerApi = PlayerApi(eventBus)
    private val battlefieldApi = BattlefieldApi(eventBus)
    private val effectApi = EffectApi(eventBus)
    private val abilityApi = AbilityApi(effectApi, eventBus)
    private val battleUnitApi = BattleUnitApi(effectApi, abilityApi, unitApi, playerApi, battlefieldApi, eventBus)
    private val battleApi = BattleApi(eventBus, battleUnitApi)
    private val cpuBrainApi = CpuBrainApi(unitApi, playerApi, battleUnitApi, battlefieldApi, eventBus)

    private val humanPlayerId = PlayerMother.id()
    private val cpuPlayerId = PlayerMother.id()
    private val deployEffectId = EffectMother.effectId()
    private val deployAbilityId = AbilityMother.id()
    private val deployerUnitId = UnitMother.id()
    private val summonUnitId = UnitMother.id()
    private val casterBattleUnitId = BattleUnitMother.id()
    private val supportBattleUnitId = BattleUnitMother.id()

    @BeforeEach
    fun setup() {
        playerApi.requestPlayerCreation(humanPlayerId, "Human", HUMAN)
        playerApi.requestPlayerCreation(cpuPlayerId, "CPU", CPU)
        battlefieldApi.initializeBattlefield(8, 8, List(8) { List(8) { "tile-id-$it" } })

        val deployEffect = EffectMother.deployBattleUnitEffect(id = deployEffectId, unitId = summonUnitId)
        effectApi.requestEffectCreation(deployEffect.toDto())

        val deployAbility =
            AbilityMother.ability(
                id = deployAbilityId,
                cost = 0,
                cooldown = 0,
                effectSpecs = listOf(AbilityMother.effectSpec(effectId = deployEffectId, target = TargetExpressionDto.Type.SELECTED_TILE)),
                targeting = Ability.Dto.TargetingDto.VACANT_TILE_ADJACENT_TO_BATTLE_UNIT,
            )
        abilityApi.requestAbilityCreation(deployAbility.toDto())

        val deployerUnit =
            UnitMother.unit(
                id = deployerUnitId,
                healthPoints = 100,
                manaPoints = 30,
                abilities = listOf(deployAbilityId),
                movementRange = 3,
            )
        val summonUnit =
            UnitMother.unit(
                id = summonUnitId,
                healthPoints = 20,
                manaPoints = 10,
                abilities = emptyList(),
                movementRange = 3,
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
        battleApi.startFirstRound(listOf(humanPlayerId, cpuPlayerId))
        eventBus.dispatch()
    }

    @Test
    fun `should deploy a new battle unit assigned to the caster player when the deploy battle unit effect is applied`() {
        // Given
        require(battlefieldApi.searchOccupant(row = 2, column = 2) == casterBattleUnitId)
        require(battlefieldApi.searchOccupant(row = 2, column = 3) == supportBattleUnitId)
        require(battleApi.searchBattle()!!.currentPlayerTurn == humanPlayerId)
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
        assertThat(humanUnitsAfter.size).isEqualTo(humanUnitsBefore + 1)
        val deployedOccupantId = battlefieldApi.searchOccupant(row = target.row, column = target.column)
        assertThat(deployedOccupantId).isNotNull()
        val deployed = battleUnitApi.searchBattleUnitById(deployedOccupantId!!)!!
        assertThat(deployed.playerId).isEqualTo(humanPlayerId)
        assertThat(deployed.unitId).isEqualTo(summonUnitId)
    }
}
