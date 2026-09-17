package com.mkz.rpg.battleUnit.usecases.commands

import com.mkz.rpg.ability.domain.Ability
import com.mkz.rpg.ability.domain.Ability.Dto.TargetExpressionDto
import com.mkz.rpg.ability.domain.AbilityMother.ability
import com.mkz.rpg.ability.domain.AbilityMother.effectSpec
import com.mkz.rpg.ability.usecases.queries.SearchAbilityById
import com.mkz.rpg.battleUnit.adapters.storage.InMemoryBattleUnitRepository
import com.mkz.rpg.battleUnit.domain.BattleUnitError
import com.mkz.rpg.battleUnit.domain.BattleUnitError.AbilityDoesNotExists
import com.mkz.rpg.battleUnit.domain.BattleUnitEvent
import com.mkz.rpg.battleUnit.domain.BattleUnitMother.battleUnit
import com.mkz.rpg.battleUnit.usecases.services.AbilityExecution
import com.mkz.rpg.battleUnit.usecases.services.DistanceService
import com.mkz.rpg.battlefield.domain.Battlefield.Dto.PositionDto
import com.mkz.rpg.battlefield.domain.BattlefieldMother
import com.mkz.rpg.battlefield.domain.BattlefieldMother.position
import com.mkz.rpg.battlefield.usecases.queries.SearchOccupant
import com.mkz.rpg.battlefield.usecases.queries.SearchPosition
import com.mkz.rpg.effect.domain.Effect.Dto.ApplicationDto
import com.mkz.rpg.effect.domain.Effect.Dto.ApplicationDto.ApplicationTypeDto
import com.mkz.rpg.effect.domain.Effect.EffectApplication
import com.mkz.rpg.effect.domain.Effect.EffectTarget
import com.mkz.rpg.effect.domain.EffectMother.decreaseHealthEffect
import com.mkz.rpg.effect.domain.EffectMother.deployBattleUnitEffect
import com.mkz.rpg.effect.domain.EffectMother.increaseHealthEffect
import com.mkz.rpg.effect.domain.EffectMother.teleportEffect
import com.mkz.rpg.effect.usecases.queries.SearchEffectById
import com.mkz.rpg.player.domain.PlayerMother.player
import com.mkz.rpg.shared.domain.FakeEventBus
import com.mkz.rpg.shared.domain.assertThat
import com.mkz.rpg.unit.domain.UnitMother.unit
import com.mkz.rpg.unit.usecases.queries.SearchUnitById
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.catchThrowable
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class RequestEffectApplicationToCastTargetsTest {
    private val searchAbilityById: SearchAbilityById = mock()
    private val searchEffectById: SearchEffectById = mock()
    private val searchOccupant: SearchOccupant = mock()
    private val searchUnitById: SearchUnitById = mock()
    private val searchPosition: SearchPosition = mock()
    private val battleUnitRepository = InMemoryBattleUnitRepository()
    private val eventBus = FakeEventBus()
    private val abilityExecution =
        AbilityExecution(
            battleUnitRepository = battleUnitRepository,
            searchPosition = searchPosition,
            searchOccupant = searchOccupant,
            searchEffectById = searchEffectById,
            distanceService = DistanceService(),
        )
    private val requestEffectApplicationToCastTargets =
        RequestEffectApplicationToCastTargets(
            searchAbilityById = searchAbilityById,
            battleUnitRepository = battleUnitRepository,
            abilityExecution = abilityExecution,
            eventBus = eventBus,
        )

    @Test
    fun `should request the effect application to the caster when the effect targets the caster`() {
        // Given
        val effectId = "effect-1"
        val effect = decreaseHealthEffect(id = effectId, damage = 3, applicationType = "IMMEDIATELY").toDto()
        val unit = unit(healthPoints = 10).toDto()
        val player = player(id = "player-1").toDto()
        val battleUnit = battleUnit(unit = unit, player = player)
        battleUnitRepository.create(battleUnit)
        val battleUnitId = battleUnit.toDto().id
        whenever(searchAbilityById("ability-1")).thenReturn(
            ability(id = "ability-1", targeting = Ability.Dto.TargetingDto.SELF, effectSpecs = listOf(effectSpec(effectId = effectId, target = TargetExpressionDto.Type.CASTER)))
                .toDto(),
        )
        whenever(searchEffectById(effectId)).thenReturn(effect)
        whenever(searchUnitById(unit.id)).thenReturn(unit)
        whenever(searchPosition(battleUnitId)).thenReturn(position())
        // When
        requestEffectApplicationToCastTargets(battleUnitId = battleUnitId, abilityId = "ability-1", row = 0, column = 0)
        // Then
        assertThat(eventBus).hasPublishedEvents(
            BattleUnitEvent.RequestApplyEffect(
                application = EffectApplication(source = battleUnitId, target = EffectTarget.Unit(id = battleUnitId), effectId = effectId),
            ),
        )
    }

    @Test
    fun `should request the effect application to the selected tile when the effect outcome is deploy battle unit`() {
        // Given
        val effectId = "effect-1"
        val effect = deployBattleUnitEffect(id = effectId, unitId = "summoned-unit").toDto()
        val unit = unit(healthPoints = 10).toDto()
        val player = player(id = "player-1").toDto()
        val battleUnit = battleUnit(unit = unit, player = player)
        battleUnitRepository.create(battleUnit)
        val battleUnitId = battleUnit.toDto().id
        whenever(searchAbilityById("ability-1")).thenReturn(
            ability(
                id = "ability-1",
                targeting = Ability.Dto.TargetingDto.VACANT_TILE_ADJACENT_TO_BATTLE_UNIT,
                effectSpecs = listOf(effectSpec(effectId = effectId, target = TargetExpressionDto.Type.SELECTED_TILE)),
            ).toDto(),
        )
        whenever(searchEffectById(effectId)).thenReturn(effect)
        whenever(searchOccupant(1, 1)).thenReturn(null)
        // When
        requestEffectApplicationToCastTargets(battleUnitId = battleUnitId, abilityId = "ability-1", row = 1, column = 1)
        // Then
        assertThat(eventBus).hasPublishedEvents(
            BattleUnitEvent.RequestApplyEffect(
                application = EffectApplication(source = battleUnitId, target = EffectTarget.Tile(row = 1, column = 1), effectId = effectId),
            ),
        )
    }

    @Test
    fun `should request the effect application to the enemy occupant when the effect targets the selected target`() {
        // Given
        val effectId = "effect-1"
        val effect = decreaseHealthEffect(id = effectId, damage = 3, applicationType = "IMMEDIATELY").toDto()
        val enemyUnit = unit(healthPoints = 10).toDto()
        val player = player(id = "player-1").toDto()
        val enemyPlayer = player(id = "player-2").toDto()
        val battleUnit = battleUnit(player = player)
        val enemyBattleUnit = battleUnit(unit = enemyUnit, player = enemyPlayer)
        battleUnitRepository.create(battleUnit)
        battleUnitRepository.create(enemyBattleUnit)
        val battleUnitId = battleUnit.toDto().id
        val enemyBattleUnitId = enemyBattleUnit.toDto().id
        whenever(searchAbilityById("ability-1")).thenReturn(
            ability(
                id = "ability-1",
                targeting = Ability.Dto.TargetingDto.ALL_ADJACENT_ENEMIES,
                effectSpecs = listOf(effectSpec(effectId = effectId, target = TargetExpressionDto.Type.SELECTED_TARGET)),
            ).toDto(),
        )
        whenever(searchEffectById(effectId)).thenReturn(effect)
        whenever(searchOccupant(1, 1)).thenReturn(enemyBattleUnitId)
        whenever(searchUnitById(enemyUnit.id)).thenReturn(enemyUnit)
        whenever(searchPosition(enemyBattleUnitId)).thenReturn(position())
        // When
        requestEffectApplicationToCastTargets(battleUnitId = battleUnitId, abilityId = "ability-1", row = 1, column = 1)
        // Then
        assertThat(eventBus).hasPublishedEvents(
            BattleUnitEvent.RequestApplyEffect(
                application = EffectApplication(source = battleUnitId, target = EffectTarget.Unit(id = enemyBattleUnitId), effectId = effectId),
            ),
        )
    }

    @Test
    fun `should request the effect application when the effect application type is on defeated`() {
        // Given
        val effectId = "effect-1"
        val effect =
            decreaseHealthEffect(id = effectId, damage = 3)
                .toDto()
                .copy(application = ApplicationDto(type = ApplicationTypeDto.ON_DEFEATED, onTurnStarted = null, beforeApplyingEffect = null))
        val unit = unit(healthPoints = 10).toDto()
        val player = player(id = "player-1").toDto()
        val battleUnit = battleUnit(unit = unit, player = player)
        battleUnitRepository.create(battleUnit)
        val battleUnitId = battleUnit.toDto().id
        whenever(searchAbilityById("ability-1")).thenReturn(
            ability(id = "ability-1", targeting = Ability.Dto.TargetingDto.SELF, effectSpecs = listOf(effectSpec(effectId = effectId, target = TargetExpressionDto.Type.CASTER)))
                .toDto(),
        )
        whenever(searchEffectById(effectId)).thenReturn(effect)
        whenever(searchUnitById(unit.id)).thenReturn(unit)
        whenever(searchPosition(battleUnitId)).thenReturn(position())
        // When
        requestEffectApplicationToCastTargets(battleUnitId = battleUnitId, abilityId = "ability-1", row = 0, column = 0)
        // Then
        assertThat(eventBus).hasPublishedEvents(
            BattleUnitEvent.RequestApplyEffect(
                application = EffectApplication(source = battleUnitId, target = EffectTarget.Unit(id = battleUnitId), effectId = effectId),
            ),
        )
    }

    @Test
    fun `should request the effect application with the teleport destination when the effect outcome is teleport`() {
        // Given
        val effectId = "effect-1"
        val effect = teleportEffect(id = effectId).toDto()
        val unit = unit(healthPoints = 10).toDto()
        val player = player(id = "player-1").toDto()
        val battleUnit = battleUnit(unit = unit, player = player)
        battleUnitRepository.create(battleUnit)
        val battleUnitId = battleUnit.toDto().id
        whenever(searchAbilityById("ability-1")).thenReturn(
            ability(
                id = "ability-1",
                targeting = Ability.Dto.TargetingDto.VACANT_TILE_ADJACENT_TO_BATTLE_UNIT,
                effectSpecs = listOf(effectSpec(effectId = effectId, target = TargetExpressionDto.Type.CASTER)),
            ).toDto(),
        )
        whenever(searchEffectById(effectId)).thenReturn(effect)
        whenever(searchOccupant(1, 1)).thenReturn(null)
        whenever(searchUnitById(unit.id)).thenReturn(unit)
        whenever(searchPosition(battleUnitId)).thenReturn(PositionDto(0, 0))
        // When
        requestEffectApplicationToCastTargets(battleUnitId = battleUnitId, abilityId = "ability-1", row = 1, column = 1)
        // Then
        assertThat(eventBus).hasPublishedEvents(
            BattleUnitEvent.RequestApplyEffect(
                application =
                    EffectApplication(
                        source = battleUnitId,
                        target = EffectTarget.Unit(id = battleUnitId),
                        effectId = effectId,
                        destination = EffectTarget.Tile(row = 1, column = 1),
                    ),
            ),
        )
    }

    @Test
    fun `should request the effect application to the defeated target when the ability targets an adjacent enemy`() {
        // Given
        val effectId = "effect-1"
        val effect = decreaseHealthEffect(id = effectId, damage = 10, applicationType = "IMMEDIATELY").toDto()
        val enemyUnit = unit(healthPoints = 3).toDto()
        val player = player(id = "player-1").toDto()
        val enemyPlayer = player(id = "player-2").toDto()
        val battleUnit = battleUnit(player = player)
        val enemyBattleUnit = battleUnit(unit = enemyUnit, player = enemyPlayer)
        battleUnitRepository.create(battleUnit)
        battleUnitRepository.create(enemyBattleUnit)
        val battleUnitId = battleUnit.toDto().id
        val enemyBattleUnitId = enemyBattleUnit.toDto().id
        val enemyPosition = BattlefieldMother.position()
        whenever(searchAbilityById("ability-1")).thenReturn(
            ability(id = "ability-1", targeting = Ability.Dto.TargetingDto.ADJACENT_ENEMY, effectSpecs = listOf(effectSpec(effectId = effectId, target = TargetExpressionDto.Type.SELECTED_TARGET)))
                .toDto(),
        )
        whenever(searchEffectById(effectId)).thenReturn(effect)
        whenever(searchOccupant(1, 1)).thenReturn(enemyBattleUnitId)
        whenever(searchUnitById(enemyUnit.id)).thenReturn(enemyUnit)
        whenever(searchPosition(enemyBattleUnitId)).thenReturn(enemyPosition)
        // When
        requestEffectApplicationToCastTargets(battleUnitId = battleUnitId, abilityId = "ability-1", row = 1, column = 1)
        // Then
        assertThat(eventBus).hasPublishedEvents(
            BattleUnitEvent.RequestApplyEffect(
                application = EffectApplication(source = battleUnitId, target = EffectTarget.Unit(id = enemyBattleUnitId), effectId = effectId),
            ),
        )
    }

    @Test
    fun `should throw ability does not exist when the ability is unknown`() {
        // Given
        val battleUnit = battleUnit()
        battleUnitRepository.create(battleUnit)
        whenever(searchAbilityById("unknown-ability")).thenReturn(null)
        // When
        val error =
            catchThrowable {
                requestEffectApplicationToCastTargets(battleUnitId = battleUnit.toDto().id, abilityId = "unknown-ability", row = 0, column = 0)
            }
        // Then
        assertThat(error).isInstanceOf(AbilityDoesNotExists::class.java)
    }

    @Test
    fun `should not receive effects when the battle unit does not exist`() {
        // Given
        val ability = ability().toDto()
        whenever(searchAbilityById(ability.id)).thenReturn(ability)
        // When
        val error = catchThrowable { requestEffectApplicationToCastTargets(battleUnitId = "unknown-battle-unit", abilityId = ability.id, row = 0, column = 0) }
        // Then
        assertThat(error).isInstanceOf(BattleUnitError.BattleUnitDoesNotExists::class.java)
    }

    @Test
    fun `should request each effect application to its own resolved target when the ability targets an enemy but an effect targets the caster`() {
        // Given
        val enemyDamageEffectId = "enemy-damage-effect"
        val casterDamageEffectId = "caster-damage-effect"
        val enemyDamageEffect = decreaseHealthEffect(id = enemyDamageEffectId, damage = 10, applicationType = "IMMEDIATELY").toDto()
        val casterDamageEffect = decreaseHealthEffect(id = casterDamageEffectId, damage = 5, applicationType = "IMMEDIATELY").toDto()
        val casterUnit = unit(healthPoints = 10).toDto()
        val enemyUnit = unit(healthPoints = 20).toDto()
        val player = player(id = "player-1").toDto()
        val enemyPlayer = player(id = "player-2").toDto()
        val caster = battleUnit(unit = casterUnit, player = player)
        val enemy = battleUnit(unit = enemyUnit, player = enemyPlayer)
        battleUnitRepository.create(caster)
        battleUnitRepository.create(enemy)
        val casterId = caster.toDto().id
        val enemyId = enemy.toDto().id
        whenever(searchAbilityById("ability-1")).thenReturn(
            ability(
                id = "ability-1",
                targeting = Ability.Dto.TargetingDto.ADJACENT_ENEMY,
                effectSpecs =
                    listOf(
                        effectSpec(effectId = enemyDamageEffectId, target = TargetExpressionDto.Type.SELECTED_TARGET),
                        effectSpec(effectId = casterDamageEffectId, target = TargetExpressionDto.Type.CASTER),
                    ),
            ).toDto(),
        )
        whenever(searchEffectById(enemyDamageEffectId)).thenReturn(enemyDamageEffect)
        whenever(searchEffectById(casterDamageEffectId)).thenReturn(casterDamageEffect)
        whenever(searchOccupant(1, 1)).thenReturn(enemyId)
        whenever(searchUnitById(casterUnit.id)).thenReturn(casterUnit)
        whenever(searchUnitById(enemyUnit.id)).thenReturn(enemyUnit)
        whenever(searchPosition(casterId)).thenReturn(position())
        whenever(searchPosition(enemyId)).thenReturn(position())
        // When
        requestEffectApplicationToCastTargets(battleUnitId = casterId, abilityId = "ability-1", row = 1, column = 1)
        // Then
        assertThat(eventBus).hasPublishedEvents(
            BattleUnitEvent.RequestApplyEffect(
                application = EffectApplication(source = casterId, target = EffectTarget.Unit(id = enemyId), effectId = enemyDamageEffectId),
            ),
            BattleUnitEvent.RequestApplyEffect(
                application = EffectApplication(source = casterId, target = EffectTarget.Unit(id = casterId), effectId = casterDamageEffectId),
            ),
        )
    }

    @Test
    fun `should request only the caster effect application when the only effect targets the caster`() {
        // Given
        val healEffectId = "heal-effect"
        val healEffect = increaseHealthEffect(id = healEffectId, healing = 10).toDto()
        val casterUnit = unit(healthPoints = 10).toDto()
        val enemyUnit = unit(healthPoints = 20).toDto()
        val player = player(id = "player-1").toDto()
        val enemyPlayer = player(id = "player-2").toDto()
        val caster = battleUnit(unit = casterUnit, player = player)
        val enemy = battleUnit(unit = enemyUnit, player = enemyPlayer)
        battleUnitRepository.create(caster)
        battleUnitRepository.create(enemy)
        val casterId = caster.toDto().id
        val enemyId = enemy.toDto().id
        whenever(searchAbilityById("ability-1")).thenReturn(
            ability(
                id = "ability-1",
                targeting = Ability.Dto.TargetingDto.ADJACENT_ENEMY,
                effectSpecs = listOf(effectSpec(effectId = healEffectId, target = TargetExpressionDto.Type.CASTER)),
            ).toDto(),
        )
        whenever(searchEffectById(healEffectId)).thenReturn(healEffect)
        whenever(searchOccupant(1, 1)).thenReturn(enemyId)
        whenever(searchUnitById(casterUnit.id)).thenReturn(casterUnit)
        // When
        requestEffectApplicationToCastTargets(battleUnitId = casterId, abilityId = "ability-1", row = 1, column = 1)
        // Then
        assertThat(eventBus).hasPublishedEvents(
            BattleUnitEvent.RequestApplyEffect(
                application = EffectApplication(source = casterId, target = EffectTarget.Unit(id = casterId), effectId = healEffectId),
            ),
        )
    }

    @Test
    fun `should request each effect application to its own resolved target when the effects target different expressions`() {
        // Given
        val damageEffectId = "damage-effect"
        val healEffectId = "heal-effect"
        val damageEffect = decreaseHealthEffect(id = damageEffectId, damage = 10, applicationType = "IMMEDIATELY").toDto()
        val healEffect = increaseHealthEffect(id = healEffectId, healing = 5).toDto()
        val casterUnit = unit(healthPoints = 10).toDto()
        val enemyUnit = unit(healthPoints = 20).toDto()
        val player = player(id = "player-1").toDto()
        val enemyPlayer = player(id = "player-2").toDto()
        val caster = battleUnit(unit = casterUnit, player = player)
        val enemy = battleUnit(unit = enemyUnit, player = enemyPlayer)
        battleUnitRepository.create(caster)
        battleUnitRepository.create(enemy)
        val casterId = caster.toDto().id
        val enemyId = enemy.toDto().id
        whenever(searchPosition(casterId)).thenReturn(position())
        whenever(searchPosition(enemyId)).thenReturn(position())
        whenever(searchEffectById(damageEffectId)).thenReturn(damageEffect)
        whenever(searchEffectById(healEffectId)).thenReturn(healEffect)
        whenever(searchUnitById(casterUnit.id)).thenReturn(casterUnit)
        whenever(searchUnitById(enemyUnit.id)).thenReturn(enemyUnit)
        whenever(searchOccupant(1, 1)).thenReturn(enemyId)
        whenever(searchAbilityById("ability-1")).thenReturn(
            ability(
                id = "ability-1",
                targeting = Ability.Dto.TargetingDto.ADJACENT_ENEMY,
                effectSpecs =
                    listOf(
                        effectSpec(effectId = damageEffectId, target = TargetExpressionDto.Type.SELECTED_TARGET),
                        effectSpec(effectId = healEffectId, target = TargetExpressionDto.Type.CASTER),
                    ),
            ).toDto(),
        )
        // When
        requestEffectApplicationToCastTargets(battleUnitId = casterId, abilityId = "ability-1", row = 1, column = 1)
        // Then
        assertThat(eventBus).hasPublishedEvents(
            BattleUnitEvent.RequestApplyEffect(
                application = EffectApplication(source = casterId, target = EffectTarget.Unit(id = enemyId), effectId = damageEffectId),
            ),
            BattleUnitEvent.RequestApplyEffect(
                application = EffectApplication(source = casterId, target = EffectTarget.Unit(id = casterId), effectId = healEffectId),
            ),
        )
    }
}
