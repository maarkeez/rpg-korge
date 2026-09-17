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

class MushroomAbilityAcceptanceTest {
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
    private val venomEffectId = EffectMother.effectId()
    private val mushroomAbilityId = AbilityMother.id()
    private val knightUnitId = UnitMother.id()
    private val ratUnitId = UnitMother.id()
    private val humanBattleUnitId = BattleUnitMother.id()
    private val firstRatBattleUnitId = BattleUnitMother.id()
    private val secondRatBattleUnitId = BattleUnitMother.id()

    @BeforeEach
    fun setup() {
        playerApi.requestPlayerCreation(humanPlayerId, "Human", HUMAN)
        playerApi.requestPlayerCreation(cpuPlayerId, "CPU", CPU)
        battlefieldApi.initializeBattlefield(8, 8, List(8) { List(8) { "tile-id-$it" } })

        val venomEffect = EffectMother.decreaseHealthEffect(id = venomEffectId)
        effectApi.requestEffectCreation(venomEffect.toDto())

        val mushroomAbility =
            AbilityMother.ability(
                id = mushroomAbilityId,
                cost = 10,
                cooldown = 0,
                effectSpecs = listOf(AbilityMother.effectSpec(effectId = venomEffectId, target = TargetExpressionDto.Type.SELECTED_TARGET)),
                targeting = Ability.Dto.TargetingDto.ALL_ADJACENT_ENEMIES,
            )
        abilityApi.requestAbilityCreation(mushroomAbility.toDto())

        val knight =
            UnitMother.unit(
                id = knightUnitId,
                healthPoints = 100,
                manaPoints = 30,
                abilities = listOf(mushroomAbilityId),
                movementRange = 3,
            )
        val rat =
            UnitMother.unit(
                id = ratUnitId,
                healthPoints = 20,
                manaPoints = 10,
                abilities = emptyList(),
                movementRange = 3,
            )
        unitApi.requestUnitCreation(knight.toDto())
        unitApi.requestUnitCreation(rat.toDto())

        // Deploy the two enemy rats adjacent to the human knight so the knight
        // can cast the mushroom ability without moving
        battleUnitApi.deployBattleUnit(
            battleUnitId = firstRatBattleUnitId,
            unitId = ratUnitId,
            playerId = cpuPlayerId,
            deployAtRow = 1,
            deployAtColumn = 0,
        )
        battleUnitApi.deployBattleUnit(
            battleUnitId = secondRatBattleUnitId,
            unitId = ratUnitId,
            playerId = cpuPlayerId,
            deployAtRow = 0,
            deployAtColumn = 1,
        )
        battleUnitApi.deployBattleUnit(
            battleUnitId = humanBattleUnitId,
            unitId = knightUnitId,
            playerId = humanPlayerId,
            deployAtRow = 1,
            deployAtColumn = 1,
        )
        battleApi.startFirstRound(listOf(humanPlayerId, cpuPlayerId))
        eventBus.dispatch()
    }

    @Test
    fun `should apply the venom effect to all adjacent enemy battle units when the mushroom ability is cast`() {
        // Given
        require(battlefieldApi.searchOccupant(row = 1, column = 1) == humanBattleUnitId)
        require(battlefieldApi.searchOccupant(row = 1, column = 0) == firstRatBattleUnitId)
        require(battlefieldApi.searchOccupant(row = 0, column = 1) == secondRatBattleUnitId)
        require(battleApi.searchBattle()!!.currentPlayerTurn == humanPlayerId)
        val castGroup = battleUnitApi.whereCanCast(humanBattleUnitId, mushroomAbilityId).single()
        battleUnitApi.castAbility(
            battleUnitId = humanBattleUnitId,
            abilityId = mushroomAbilityId,
            castGroup = castGroup,
        )
        // When
        eventBus.dispatch()
        // Then
        val firstRat = battleUnitApi.searchBattleUnitById(firstRatBattleUnitId)!!
        assertThat(firstRat.ongoingEffects.onTurnStarted).contains(venomEffectId)
        val secondRat = battleUnitApi.searchBattleUnitById(secondRatBattleUnitId)!!
        assertThat(secondRat.ongoingEffects.onTurnStarted).contains(venomEffectId)
    }
}
