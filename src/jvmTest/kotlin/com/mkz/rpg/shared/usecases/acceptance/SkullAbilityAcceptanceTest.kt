package com.mkz.rpg.shared.usecases.acceptance

import com.mkz.rpg.ability.adapters.presentation.AbilityApi
import com.mkz.rpg.ability.domain.Ability
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

class SkullAbilityAcceptanceTest {
    private val eventBus = InMemoryEventBus()
    private val unitApi = UnitApi(eventBus)
    private val playerApi = PlayerApi(eventBus)
    private val battlefieldApi = BattlefieldApi(eventBus)
    private val effectApi = EffectApi(eventBus)
    private val abilityApi = AbilityApi(effectApi, eventBus)
    private val battleUnitApi = BattleUnitApi(effectApi, abilityApi, unitApi, playerApi, battlefieldApi, eventBus)
    private val battleApi = BattleApi(eventBus, battleUnitApi)
    private val cpuBrainApi = CpuBrainApi(unitApi, playerApi, battleUnitApi, battleApi, battlefieldApi, eventBus)

    private val humanPlayerId = PlayerMother.id()
    private val cpuPlayerId = PlayerMother.id()
    private val venomEffectId = EffectMother.effectId()
    private val skullEffectId = EffectMother.effectId()
    private val swordDamageEffectId = EffectMother.effectId()
    private val healEffectId = EffectMother.effectId()
    private val skullAbilityId = AbilityMother.id()
    private val swordAbilityId = AbilityMother.id()
    private val healAbilityId = AbilityMother.id()
    private val skullUnitId = UnitMother.id()
    private val swordUnitId = UnitMother.id()
    private val enemyUnitId = UnitMother.id()
    private val allyUnitId = UnitMother.id()
    private val skullBattleUnitId = BattleUnitMother.id()
    private val swordBattleUnitId = BattleUnitMother.id()
    private val enemyBattleUnitId = BattleUnitMother.id()
    private val allyBattleUnitId = BattleUnitMother.id()

    @BeforeEach
    fun setup() {
        playerApi.requestPlayerCreation(humanPlayerId, "Human", HUMAN)
        playerApi.requestPlayerCreation(cpuPlayerId, "CPU", CPU)
        battlefieldApi.initializeBattlefield(8, 8, List(8) { List(8) { "tile-id-$it" } })

        val venomEffect = EffectMother.decreaseHealthEffect(id = venomEffectId, damage = 3, applicationType = "ON_TURN_STARTED")
        val skullEffect = EffectMother.applyEffectOnNearbyAlliesEffect(id = skullEffectId, effectId = venomEffectId)
        val swordDamageEffect = EffectMother.decreaseHealthEffect(id = swordDamageEffectId, damage = 10, applicationType = "IMMEDIATELY")
        val healEffect = EffectMother.increaseHealthEffect(id = healEffectId, healing = 20)
        effectApi.requestEffectCreation(venomEffect.toDto())
        effectApi.requestEffectCreation(skullEffect.toDto())
        effectApi.requestEffectCreation(swordDamageEffect.toDto())
        effectApi.requestEffectCreation(healEffect.toDto())

        val skullAbility =
            AbilityMother.ability(
                id = skullAbilityId,
                cost = 0,
                cooldown = 0,
                effects = listOf(skullEffectId),
                targetPattern = Ability.Dto.TargetPatternDto.ADJACENT_ENEMY,
            )
        val swordAbility =
            AbilityMother.ability(
                id = swordAbilityId,
                cost = 0,
                cooldown = 0,
                effects = listOf(swordDamageEffectId),
                targetPattern = Ability.Dto.TargetPatternDto.ADJACENT_ENEMY,
            )
        val healAbility =
            AbilityMother.ability(
                id = healAbilityId,
                cost = 10,
                cooldown = 2,
                effects = listOf(healEffectId),
                targetPattern = Ability.Dto.TargetPatternDto.SELF,
            )
        abilityApi.requestAbilityCreation(skullAbility.toDto())
        abilityApi.requestAbilityCreation(swordAbility.toDto())
        abilityApi.requestAbilityCreation(healAbility.toDto())

        val skullUnit =
            UnitMother.unit(
                id = skullUnitId,
                healthPoints = 100,
                manaPoints = 30,
                abilities = listOf(skullAbilityId),
                movementRange = 3,
            )
        val swordUnit =
            UnitMother.unit(
                id = swordUnitId,
                healthPoints = 100,
                manaPoints = 30,
                abilities = listOf(swordAbilityId),
                movementRange = 3,
            )
        val enemyUnit =
            UnitMother.unit(
                id = enemyUnitId,
                healthPoints = 5,
                manaPoints = 1,
                abilities = emptyList(),
                movementRange = 3,
            )
        val allyUnit =
            UnitMother.unit(
                id = allyUnitId,
                healthPoints = 100,
                manaPoints = 30,
                abilities = listOf(healAbilityId),
                movementRange = 3,
            )
        unitApi.requestUnitCreation(skullUnit.toDto())
        unitApi.requestUnitCreation(swordUnit.toDto())
        unitApi.requestUnitCreation(enemyUnit.toDto())
        unitApi.requestUnitCreation(allyUnit.toDto())

        battleUnitApi.deployBattleUnit(
            battleUnitId = skullBattleUnitId,
            unitId = skullUnitId,
            playerId = humanPlayerId,
            deployAtRow = 1,
            deployAtColumn = 1,
        )
        battleUnitApi.deployBattleUnit(
            battleUnitId = swordBattleUnitId,
            unitId = swordUnitId,
            playerId = humanPlayerId,
            deployAtRow = 2,
            deployAtColumn = 2,
        )
        battleUnitApi.deployBattleUnit(
            battleUnitId = enemyBattleUnitId,
            unitId = enemyUnitId,
            playerId = cpuPlayerId,
            deployAtRow = 1,
            deployAtColumn = 2,
        )
        battleUnitApi.deployBattleUnit(
            battleUnitId = allyBattleUnitId,
            unitId = allyUnitId,
            playerId = cpuPlayerId,
            deployAtRow = 0,
            deployAtColumn = 2,
        )
        battleApi.startFirstRound(listOf(humanPlayerId, cpuPlayerId))
        eventBus.dispatch()
    }

    @Test
    fun `should apply the effect to the nearby allies when the battle unit with the on defeated effect is defeated`() {
        // Given
        require(battlefieldApi.searchOccupant(row = 1, column = 1) == skullBattleUnitId)
        require(battlefieldApi.searchOccupant(row = 2, column = 2) == swordBattleUnitId)
        require(battlefieldApi.searchOccupant(row = 1, column = 2) == enemyBattleUnitId)
        require(battlefieldApi.searchOccupant(row = 0, column = 2) == allyBattleUnitId)
        require(battleApi.searchBattle()!!.currentPlayerTurn == humanPlayerId)
        val skullCastGroup = battleUnitApi.whereCanCast(skullBattleUnitId, skullAbilityId).single()
        val swordCastGroup = battleUnitApi.whereCanCast(swordBattleUnitId, swordAbilityId).single()
        battleUnitApi.castAbility(
            battleUnitId = skullBattleUnitId,
            abilityId = skullAbilityId,
            castGroup = skullCastGroup,
        )
        battleUnitApi.castAbility(
            battleUnitId = swordBattleUnitId,
            abilityId = swordAbilityId,
            castGroup = swordCastGroup,
        )
        // When
        eventBus.dispatch()
        // Then
        val enemy = battleUnitApi.searchBattleUnitById(enemyBattleUnitId)!!
        assertThat(enemy.remainingHealthPoints).isEqualTo(0)
        val ally = battleUnitApi.searchBattleUnitById(allyBattleUnitId)!!
        assertThat(ally.ongoingEffects.delayedEffects).contains(venomEffectId)
    }
}
