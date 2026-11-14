package com.mkz.tessavar.battleUnit.usecases.commands

import com.mkz.tessavar.ability.domain.Ability
import com.mkz.tessavar.ability.domain.Ability.Dto.EffectTargetDto
import com.mkz.tessavar.ability.domain.AbilityMother.ability
import com.mkz.tessavar.ability.domain.AbilityMother.abilityEffect
import com.mkz.tessavar.battleUnit.domain.BattleUnitError
import com.mkz.tessavar.battleUnit.domain.BattleUnitError.AbilityDoesNotExists
import com.mkz.tessavar.battleUnit.domain.BattleUnitEvent
import com.mkz.tessavar.battleUnit.domain.BattleUnitMother.battleUnit
import com.mkz.tessavar.battlefield.domain.Battlefield
import com.mkz.tessavar.battlefield.domain.BattlefieldMother.battlefield
import com.mkz.tessavar.effect.domain.Effect
import com.mkz.tessavar.effect.domain.Effect.Dto.EffectApplicationDto
import com.mkz.tessavar.effect.domain.Effect.Dto.EffectApplicationDto.SourceDto
import com.mkz.tessavar.effect.domain.Effect.Dto.RecipientDto
import com.mkz.tessavar.effect.domain.Effect.Dto.TimingDto
import com.mkz.tessavar.effect.domain.Effect.Dto.TimingDto.TimingTypeDto
import com.mkz.tessavar.effect.domain.EffectMother.damageEffect
import com.mkz.tessavar.effect.domain.EffectMother.healEffect
import com.mkz.tessavar.effect.domain.EffectMother.summonEffect
import com.mkz.tessavar.effect.domain.EffectMother.teleportEffect
import com.mkz.tessavar.player.domain.PlayerMother.player
import com.mkz.tessavar.shared.domain.FakeEventBus
import com.mkz.tessavar.shared.domain.shouldHavePublishedEvents
import com.mkz.tessavar.shared.usecases.InMemoryQueries
import com.mkz.tessavar.unit.domain.UnitMother.unit
import io.kotest.assertions.throwables.shouldThrowAny
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlin.test.Test

class RequestEffectApplicationToCastTargetsTest {
    private val queries = InMemoryQueries()
    private val battleUnitRepository = queries.battleUnitRepository
    private val eventBus = FakeEventBus()
    private val requestEffectApplicationToCastTargets =
        RequestEffectApplicationToCastTargets(
            searchAbilityById = queries.searchAbilityById,
            battleUnitRepository = battleUnitRepository,
            castResolution = queries.castResolution,
            eventBus = eventBus,
        )

    @Test
    fun `should request the effect application to the caster when the effect targets the caster`() {
        // Given
        val effectId = "effect-1"
        val effect = damageEffect(id = effectId, damage = 3, timingType = "IMMEDIATELY").toDto()
        val unit = unit(baseMaximumHealth = 10).toDto()
        val player = player(id = "player-1").toDto()
        val battleUnit = battleUnit(unit = unit, player = player)
        battleUnitRepository.create(battleUnit)
        val battleUnitId = battleUnit.toDto().id
        queries.abilityRepository.create(
            ability(id = "ability-1", targeting = Ability.Dto.TargetingDto.CASTER, abilityEffects = listOf(abilityEffect(effectId = effectId, target = EffectTargetDto.Type.CASTER))),
        )
        queries.effectRepository.create(Effect.create(effect))
        queries.battlefieldRepository.create(battlefield("C .", occupants = mapOf('C' to battleUnitId)))
        // When
        requestEffectApplicationToCastTargets(battleUnitId = battleUnitId, abilityId = "ability-1", castGroup = listOf(Battlefield.Dto.PositionDto(row = 0, column = 0)))
        // Then
        eventBus.shouldHavePublishedEvents(
            BattleUnitEvent.RequestApplyEffect(
                application =
                    EffectApplicationDto(
                        source = SourceDto.battleUnit(battleUnitId, abilityId = "ability-1"),
                        recipient = RecipientDto.battleUnit(battleUnitId),
                        effectId = effectId,
                        destination = null,
                    ),
            ),
            BattleUnitEvent.AbilityCastResolved(battleUnitId = battleUnitId, abilityId = "ability-1"),
        )
    }

    @Test
    fun `should request the effect application to the selected tile when the effect outcome is deploy battle unit`() {
        // Given
        val effectId = "effect-1"
        val effect = summonEffect(id = effectId, unitId = "summoned-unit").toDto()
        val unit = unit(baseMaximumHealth = 10).toDto()
        val player = player(id = "player-1").toDto()
        val battleUnit = battleUnit(unit = unit, player = player)
        battleUnitRepository.create(battleUnit)
        val battleUnitId = battleUnit.toDto().id
        queries.abilityRepository.create(
            ability(
                id = "ability-1",
                targeting = Ability.Dto.TargetingDto.VACANT_TILE_ADJACENT_TO_ANOTHER_BATTLE_UNIT,
                abilityEffects = listOf(abilityEffect(effectId = effectId, target = EffectTargetDto.Type.TARGETED_TILE)),
            ),
        )
        queries.effectRepository.create(Effect.create(effect))
        queries.battlefieldRepository.create(battlefield("C .\n. .", occupants = mapOf('C' to battleUnitId)))
        // When
        requestEffectApplicationToCastTargets(battleUnitId = battleUnitId, abilityId = "ability-1", castGroup = listOf(Battlefield.Dto.PositionDto(row = 1, column = 1)))
        // Then
        eventBus.shouldHavePublishedEvents(
            BattleUnitEvent.RequestApplyEffect(
                application =
                    EffectApplicationDto(
                        source = SourceDto.battleUnit(battleUnitId, abilityId = "ability-1"),
                        recipient = RecipientDto.position(row = 1, column = 1),
                        effectId = effectId,
                        destination = null,
                    ),
            ),
            BattleUnitEvent.AbilityCastResolved(battleUnitId = battleUnitId, abilityId = "ability-1"),
        )
    }

    @Test
    fun `should request the effect application to the enemy occupant when the effect targets the selected target`() {
        // Given
        val effectId = "effect-1"
        val effect = damageEffect(id = effectId, damage = 3, timingType = "IMMEDIATELY").toDto()
        val enemyUnit = unit(baseMaximumHealth = 10).toDto()
        val player = player(id = "player-1").toDto()
        val opponent = player(id = "player-2").toDto()
        val battleUnit = battleUnit(player = player)
        val enemyBattleUnit = battleUnit(unit = enemyUnit, player = opponent)
        battleUnitRepository.create(battleUnit)
        battleUnitRepository.create(enemyBattleUnit)
        val battleUnitId = battleUnit.toDto().id
        val enemyBattleUnitId = enemyBattleUnit.toDto().id
        queries.abilityRepository.create(
            ability(
                id = "ability-1",
                targeting = Ability.Dto.TargetingDto.ALL_ADJACENT_ENEMIES,
                abilityEffects = listOf(abilityEffect(effectId = effectId, target = EffectTargetDto.Type.TARGETED_BATTLE_UNIT)),
            ),
        )
        queries.effectRepository.create(Effect.create(effect))
        queries.battlefieldRepository.create(battlefield("C .\n. E", occupants = mapOf('C' to battleUnitId, 'E' to enemyBattleUnitId)))
        // When
        requestEffectApplicationToCastTargets(battleUnitId = battleUnitId, abilityId = "ability-1", castGroup = listOf(Battlefield.Dto.PositionDto(row = 1, column = 1)))
        // Then
        eventBus.shouldHavePublishedEvents(
            BattleUnitEvent.RequestApplyEffect(
                application =
                    EffectApplicationDto(
                        source = SourceDto.battleUnit(battleUnitId, abilityId = "ability-1"),
                        recipient = RecipientDto.battleUnit(enemyBattleUnitId),
                        effectId = effectId,
                        destination = null,
                    ),
            ),
            BattleUnitEvent.AbilityCastResolved(battleUnitId = battleUnitId, abilityId = "ability-1"),
        )
    }

    @Test
    fun `should request the effect application when the timing is on defeated`() {
        // Given
        val effectId = "effect-1"
        val effect =
            damageEffect(id = effectId, damage = 3)
                .toDto()
                .copy(timing = TimingDto(type = TimingTypeDto.ON_DEFEATED, onPlayerTurnStarted = null, beforeApplyingEffect = null))
        val unit = unit(baseMaximumHealth = 10).toDto()
        val player = player(id = "player-1").toDto()
        val battleUnit = battleUnit(unit = unit, player = player)
        battleUnitRepository.create(battleUnit)
        val battleUnitId = battleUnit.toDto().id
        queries.abilityRepository.create(
            ability(id = "ability-1", targeting = Ability.Dto.TargetingDto.CASTER, abilityEffects = listOf(abilityEffect(effectId = effectId, target = EffectTargetDto.Type.CASTER))),
        )
        queries.effectRepository.create(Effect.create(effect))
        queries.battlefieldRepository.create(battlefield("C .", occupants = mapOf('C' to battleUnitId)))
        // When
        requestEffectApplicationToCastTargets(battleUnitId = battleUnitId, abilityId = "ability-1", castGroup = listOf(Battlefield.Dto.PositionDto(row = 0, column = 0)))
        // Then
        eventBus.shouldHavePublishedEvents(
            BattleUnitEvent.RequestApplyEffect(
                application =
                    EffectApplicationDto(
                        source = SourceDto.battleUnit(battleUnitId, abilityId = "ability-1"),
                        recipient = RecipientDto.battleUnit(battleUnitId),
                        effectId = effectId,
                        destination = null,
                    ),
            ),
            BattleUnitEvent.AbilityCastResolved(battleUnitId = battleUnitId, abilityId = "ability-1"),
        )
    }

    @Test
    fun `should request the effect application with the teleport destination when the effect outcome is teleport`() {
        // Given
        val effectId = "effect-1"
        val effect = teleportEffect(id = effectId).toDto()
        val unit = unit(baseMaximumHealth = 10).toDto()
        val player = player(id = "player-1").toDto()
        val battleUnit = battleUnit(unit = unit, player = player)
        battleUnitRepository.create(battleUnit)
        val battleUnitId = battleUnit.toDto().id
        queries.abilityRepository.create(
            ability(
                id = "ability-1",
                targeting = Ability.Dto.TargetingDto.VACANT_TILE_ADJACENT_TO_ANOTHER_BATTLE_UNIT,
                abilityEffects = listOf(abilityEffect(effectId = effectId, target = EffectTargetDto.Type.CASTER)),
            ),
        )
        queries.effectRepository.create(Effect.create(effect))
        queries.battlefieldRepository.create(battlefield("C .\n. .", occupants = mapOf('C' to battleUnitId)))
        // When
        requestEffectApplicationToCastTargets(battleUnitId = battleUnitId, abilityId = "ability-1", castGroup = listOf(Battlefield.Dto.PositionDto(row = 1, column = 1)))
        // Then
        eventBus.shouldHavePublishedEvents(
            BattleUnitEvent.RequestApplyEffect(
                application =
                    EffectApplicationDto(
                        source = SourceDto.battleUnit(battleUnitId, abilityId = "ability-1"),
                        recipient = RecipientDto.battleUnit(battleUnitId),
                        effectId = effectId,
                        destination = RecipientDto.position(row = 1, column = 1),
                    ),
            ),
            BattleUnitEvent.AbilityCastResolved(battleUnitId = battleUnitId, abilityId = "ability-1"),
        )
    }

    @Test
    fun `should request the effect application to the defeated target when the ability targets an adjacent enemy`() {
        // Given
        val effectId = "effect-1"
        val effect = damageEffect(id = effectId, damage = 10, timingType = "IMMEDIATELY").toDto()
        val enemyUnit = unit(baseMaximumHealth = 3).toDto()
        val player = player(id = "player-1").toDto()
        val opponent = player(id = "player-2").toDto()
        val battleUnit = battleUnit(player = player)
        val enemyBattleUnit = battleUnit(unit = enemyUnit, player = opponent)
        battleUnitRepository.create(battleUnit)
        battleUnitRepository.create(enemyBattleUnit)
        val battleUnitId = battleUnit.toDto().id
        val enemyBattleUnitId = enemyBattleUnit.toDto().id
        queries.abilityRepository.create(
            ability(
                id = "ability-1",
                targeting = Ability.Dto.TargetingDto.ADJACENT_ENEMY,
                abilityEffects = listOf(abilityEffect(effectId = effectId, target = EffectTargetDto.Type.TARGETED_BATTLE_UNIT)),
            ),
        )
        queries.effectRepository.create(Effect.create(effect))
        queries.battlefieldRepository.create(battlefield("C .\n. E", occupants = mapOf('C' to battleUnitId, 'E' to enemyBattleUnitId)))
        // When
        requestEffectApplicationToCastTargets(battleUnitId = battleUnitId, abilityId = "ability-1", castGroup = listOf(Battlefield.Dto.PositionDto(row = 1, column = 1)))
        // Then
        eventBus.shouldHavePublishedEvents(
            BattleUnitEvent.RequestApplyEffect(
                application =
                    EffectApplicationDto(
                        source = SourceDto.battleUnit(battleUnitId, abilityId = "ability-1"),
                        recipient = RecipientDto.battleUnit(enemyBattleUnitId),
                        effectId = effectId,
                        destination = null,
                    ),
            ),
            BattleUnitEvent.AbilityCastResolved(battleUnitId = battleUnitId, abilityId = "ability-1"),
        )
    }

    @Test
    fun `should throw ability does not exist when the ability is unknown`() {
        // Given
        val battleUnit = battleUnit()
        battleUnitRepository.create(battleUnit)
        // When
        val error =
            shouldThrowAny {
                requestEffectApplicationToCastTargets(battleUnitId = battleUnit.toDto().id, abilityId = "unknown-ability", castGroup = listOf(Battlefield.Dto.PositionDto(row = 0, column = 0)))
            }
        // Then
        error.shouldBeInstanceOf<AbilityDoesNotExists>()
    }

    @Test
    fun `should not receive effects when the battle unit does not exist`() {
        // Given
        val ability = ability().also(queries.abilityRepository::create).toDto()
        // When
        val error =
            shouldThrowAny { requestEffectApplicationToCastTargets(battleUnitId = "unknown-battle-unit", abilityId = ability.id, castGroup = listOf(Battlefield.Dto.PositionDto(row = 0, column = 0))) }
        // Then
        error.shouldBeInstanceOf<BattleUnitError.BattleUnitDoesNotExists>()
    }

    @Test
    fun `should request each effect application to its own resolved target when the ability targets an enemy but an effect targets the caster`() {
        // Given
        val enemyDamageEffectId = "enemy-damage-effect"
        val casterDamageEffectId = "caster-damage-effect"
        val enemyDamageEffect = damageEffect(id = enemyDamageEffectId, damage = 10, timingType = "IMMEDIATELY").toDto()
        val casterDamageEffect = damageEffect(id = casterDamageEffectId, damage = 5, timingType = "IMMEDIATELY").toDto()
        val casterUnit = unit(baseMaximumHealth = 10).toDto()
        val enemyUnit = unit(baseMaximumHealth = 20).toDto()
        val player = player(id = "player-1").toDto()
        val opponent = player(id = "player-2").toDto()
        val caster = battleUnit(unit = casterUnit, player = player)
        val enemy = battleUnit(unit = enemyUnit, player = opponent)
        battleUnitRepository.create(caster)
        battleUnitRepository.create(enemy)
        val casterId = caster.toDto().id
        val enemyId = enemy.toDto().id
        queries.abilityRepository.create(
            ability(
                id = "ability-1",
                targeting = Ability.Dto.TargetingDto.ADJACENT_ENEMY,
                abilityEffects =
                    listOf(
                        abilityEffect(effectId = enemyDamageEffectId, target = EffectTargetDto.Type.TARGETED_BATTLE_UNIT),
                        abilityEffect(effectId = casterDamageEffectId, target = EffectTargetDto.Type.CASTER),
                    ),
            ),
        )
        queries.effectRepository.create(Effect.create(enemyDamageEffect))
        queries.effectRepository.create(Effect.create(casterDamageEffect))
        queries.battlefieldRepository.create(battlefield("C .\n. E", occupants = mapOf('C' to casterId, 'E' to enemyId)))
        // When
        requestEffectApplicationToCastTargets(battleUnitId = casterId, abilityId = "ability-1", castGroup = listOf(Battlefield.Dto.PositionDto(row = 1, column = 1)))
        // Then
        eventBus.shouldHavePublishedEvents(
            BattleUnitEvent.RequestApplyEffect(
                application =
                    EffectApplicationDto(
                        source = SourceDto.battleUnit(casterId, abilityId = "ability-1"),
                        recipient = RecipientDto.battleUnit(enemyId),
                        effectId = enemyDamageEffectId,
                        destination = null,
                    ),
            ),
            BattleUnitEvent.RequestApplyEffect(
                application =
                    EffectApplicationDto(
                        source = SourceDto.battleUnit(casterId, abilityId = "ability-1"),
                        recipient = RecipientDto.battleUnit(casterId),
                        effectId = casterDamageEffectId,
                        destination = null,
                    ),
            ),
            BattleUnitEvent.AbilityCastResolved(battleUnitId = casterId, abilityId = "ability-1"),
        )
    }

    @Test
    fun `should request only the caster effect application when the only effect targets the caster`() {
        // Given
        val healEffectId = "heal-effect"
        val healEffect = healEffect(id = healEffectId, healing = 10).toDto()
        val casterUnit = unit(baseMaximumHealth = 10).toDto()
        val enemyUnit = unit(baseMaximumHealth = 20).toDto()
        val player = player(id = "player-1").toDto()
        val opponent = player(id = "player-2").toDto()
        val caster = battleUnit(unit = casterUnit, player = player)
        val enemy = battleUnit(unit = enemyUnit, player = opponent)
        battleUnitRepository.create(caster)
        battleUnitRepository.create(enemy)
        val casterId = caster.toDto().id
        val enemyId = enemy.toDto().id
        queries.abilityRepository.create(
            ability(
                id = "ability-1",
                targeting = Ability.Dto.TargetingDto.ADJACENT_ENEMY,
                abilityEffects = listOf(abilityEffect(effectId = healEffectId, target = EffectTargetDto.Type.CASTER)),
            ),
        )
        queries.effectRepository.create(Effect.create(healEffect))
        queries.battlefieldRepository.create(battlefield("C .\n. E", occupants = mapOf('C' to casterId, 'E' to enemyId)))
        // When
        requestEffectApplicationToCastTargets(battleUnitId = casterId, abilityId = "ability-1", castGroup = listOf(Battlefield.Dto.PositionDto(row = 1, column = 1)))
        // Then
        eventBus.shouldHavePublishedEvents(
            BattleUnitEvent.RequestApplyEffect(
                application =
                    EffectApplicationDto(
                        source = SourceDto.battleUnit(casterId, abilityId = "ability-1"),
                        recipient = RecipientDto.battleUnit(casterId),
                        effectId = healEffectId,
                        destination = null,
                    ),
            ),
            BattleUnitEvent.AbilityCastResolved(battleUnitId = casterId, abilityId = "ability-1"),
        )
    }

    @Test
    fun `should request each effect application to its own resolved target when the effects have different effect targets`() {
        // Given
        val damageEffectId = "damage-effect"
        val healEffectId = "heal-effect"
        val damageEffect = damageEffect(id = damageEffectId, damage = 10, timingType = "IMMEDIATELY").toDto()
        val healEffect = healEffect(id = healEffectId, healing = 5).toDto()
        val casterUnit = unit(baseMaximumHealth = 10).toDto()
        val enemyUnit = unit(baseMaximumHealth = 20).toDto()
        val player = player(id = "player-1").toDto()
        val opponent = player(id = "player-2").toDto()
        val caster = battleUnit(unit = casterUnit, player = player)
        val enemy = battleUnit(unit = enemyUnit, player = opponent)
        battleUnitRepository.create(caster)
        battleUnitRepository.create(enemy)
        val casterId = caster.toDto().id
        val enemyId = enemy.toDto().id
        queries.effectRepository.create(Effect.create(damageEffect))
        queries.effectRepository.create(Effect.create(healEffect))
        queries.abilityRepository.create(
            ability(
                id = "ability-1",
                targeting = Ability.Dto.TargetingDto.ADJACENT_ENEMY,
                abilityEffects =
                    listOf(
                        abilityEffect(effectId = damageEffectId, target = EffectTargetDto.Type.TARGETED_BATTLE_UNIT),
                        abilityEffect(effectId = healEffectId, target = EffectTargetDto.Type.CASTER),
                    ),
            ),
        )
        queries.battlefieldRepository.create(battlefield("C .\n. E", occupants = mapOf('C' to casterId, 'E' to enemyId)))
        // When
        requestEffectApplicationToCastTargets(battleUnitId = casterId, abilityId = "ability-1", castGroup = listOf(Battlefield.Dto.PositionDto(row = 1, column = 1)))
        // Then
        eventBus.shouldHavePublishedEvents(
            BattleUnitEvent.RequestApplyEffect(
                application =
                    EffectApplicationDto(
                        source = SourceDto.battleUnit(casterId, abilityId = "ability-1"),
                        recipient = RecipientDto.battleUnit(enemyId),
                        effectId = damageEffectId,
                        destination = null,
                    ),
            ),
            BattleUnitEvent.RequestApplyEffect(
                application =
                    EffectApplicationDto(
                        source = SourceDto.battleUnit(casterId, abilityId = "ability-1"),
                        recipient = RecipientDto.battleUnit(casterId),
                        effectId = healEffectId,
                        destination = null,
                    ),
            ),
            BattleUnitEvent.AbilityCastResolved(battleUnitId = casterId, abilityId = "ability-1"),
        )
    }
}
