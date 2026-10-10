package com.mkz.rpg.battleUnit.usecases.queries

import com.mkz.rpg.ability.domain.Ability
import com.mkz.rpg.ability.domain.AbilityMother.ability
import com.mkz.rpg.ability.usecases.queries.SearchAbilityById
import com.mkz.rpg.battleUnit.adapters.storage.InMemoryBattleUnitRepository
import com.mkz.rpg.battleUnit.domain.BattleUnit
import com.mkz.rpg.battleUnit.domain.BattleUnitMother.battleUnit
import com.mkz.rpg.battleUnit.usecases.queries.PreviewAbilityCast.AppliedEffectPreview
import com.mkz.rpg.battleUnit.usecases.queries.PreviewAbilityCast.AppliedEffectPreview.Timing
import com.mkz.rpg.battleUnit.usecases.queries.PreviewAbilityCast.TilePreview
import com.mkz.rpg.battleUnit.usecases.services.AbilityExecution
import com.mkz.rpg.battlefield.domain.Battlefield.Dto.PositionDto
import com.mkz.rpg.battlefield.usecases.queries.SearchPosition
import com.mkz.rpg.effect.domain.Effect
import com.mkz.rpg.effect.domain.Effect.Dto.EffectApplicationDto
import com.mkz.rpg.effect.domain.Effect.Dto.EffectApplicationDto.ApplicationSourceDto
import com.mkz.rpg.effect.domain.Effect.Dto.EffectTargetDto
import com.mkz.rpg.effect.domain.EffectMother.applyEffectOnNearbyAlliesEffect
import com.mkz.rpg.effect.domain.EffectMother.decreaseHealthEffect
import com.mkz.rpg.effect.domain.EffectMother.deployBattleUnitEffect
import com.mkz.rpg.effect.domain.EffectMother.increaseHealthEffect
import com.mkz.rpg.effect.domain.EffectMother.teleportEffect
import com.mkz.rpg.effect.usecases.queries.SearchEffectById
import com.mkz.rpg.unit.domain.UnitMother.unit
import com.mkz.rpg.unit.usecases.queries.SearchUnitById
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class PreviewAbilityCastTest {
    private val battleUnitRepository = InMemoryBattleUnitRepository()
    private val searchAbilityById: SearchAbilityById = mock()
    private val searchEffectById: SearchEffectById = mock()
    private val searchUnitById: SearchUnitById = mock()
    private val searchPosition: SearchPosition = mock()
    private val abilityExecution: AbilityExecution = mock()
    private val previewAbilityCast =
        PreviewAbilityCast(
            battleUnitRepository = battleUnitRepository,
            searchAbilityById = searchAbilityById,
            searchEffectById = searchEffectById,
            searchUnitById = searchUnitById,
            searchPosition = searchPosition,
            abilityExecution = abilityExecution,
        )

    private val casterUnit = unit(manaPoints = 30)
    private val caster = battleUnit(unit = casterUnit.toDto())
    private val targetUnit = unit(healthPoints = 20)
    private val target = battleUnit(unit = targetUnit.toDto())

    init {
        battleUnitRepository.create(caster)
        battleUnitRepository.create(target)
        whenever(searchUnitById(casterUnit.toDto().id)).thenReturn(casterUnit.toDto())
        whenever(searchUnitById(targetUnit.toDto().id)).thenReturn(targetUnit.toDto())
        whenever(searchPosition(any())).thenReturn(PositionDto(row = 1, column = 1))
    }

    @Test
    fun `should predict the damage when the ability deals immediate damage`() {
        // Given
        val damageEffect = decreaseHealthEffect(damage = 10, applicationType = "IMMEDIATELY").toDto()
        val castAbility = givenAbility(cost = 0, effects = listOf(damageEffect))
        givenApplications(row = 1, column = 2, applications = listOf(onUnit(target, damageEffect)))
        // When
        val preview = previewAbilityCast(caster.id(), castAbility.id, listOf(PositionDto(1, 2)))
        // Then
        val targetPreview = preview.targets.single()
        assertThat(targetPreview.battleUnitId).isEqualTo(target.id())
        assertThat(targetPreview.hpBefore).isEqualTo(20)
        assertThat(targetPreview.hpAfter).isEqualTo(10)
        assertThat(targetPreview.maxHp).isEqualTo(20)
        assertThat(targetPreview.isLethal).isFalse()
    }

    @Test
    fun `should predict a lethal hit when the damage reaches zero health`() {
        // Given
        val damageEffect = decreaseHealthEffect(damage = 50, applicationType = "IMMEDIATELY").toDto()
        val castAbility = givenAbility(cost = 0, effects = listOf(damageEffect))
        givenApplications(row = 1, column = 2, applications = listOf(onUnit(target, damageEffect)))
        // When
        val preview = previewAbilityCast(caster.id(), castAbility.id, listOf(PositionDto(1, 2)))
        // Then
        val targetPreview = preview.targets.single()
        assertThat(targetPreview.hpAfter).isZero()
        assertThat(targetPreview.isLethal).isTrue()
    }

    @Test
    fun `should predict the damage and the on death effect when the ability applies both`() {
        // Given
        val damageEffect = decreaseHealthEffect(damage = 10, applicationType = "IMMEDIATELY").toDto()
        val onDeathEffect = applyEffectOnNearbyAlliesEffect().toDto()
        val castAbility = givenAbility(cost = 0, effects = listOf(damageEffect, onDeathEffect))
        givenApplications(row = 1, column = 2, applications = listOf(onUnit(target, damageEffect), onUnit(target, onDeathEffect)))
        // When
        val preview = previewAbilityCast(caster.id(), castAbility.id, listOf(PositionDto(1, 2)))
        // Then
        val targetPreview = preview.targets.single()
        assertThat(targetPreview.hpAfter).isEqualTo(10)
        assertThat(targetPreview.appliedEffects.filter { it.timing == Timing.ON_DEATH }).containsExactly(AppliedEffectPreview(onDeathEffect.id, Timing.ON_DEATH, perTurn = null, turns = null))
    }

    @Test
    fun `should predict an over time effect without changing the health when the effect applies on turn started`() {
        // Given
        val venomEffect = decreaseHealthEffect(damage = 3).toDto()
        val castAbility = givenAbility(cost = 0, effects = listOf(venomEffect))
        givenApplications(row = 1, column = 2, applications = listOf(onUnit(target, venomEffect)))
        // When
        val preview = previewAbilityCast(caster.id(), castAbility.id, listOf(PositionDto(1, 2)))
        // Then
        val targetPreview = preview.targets.single()
        assertThat(targetPreview.hpAfter).isEqualTo(20)
        assertThat(targetPreview.appliedEffects).containsExactly(AppliedEffectPreview(venomEffect.id, Timing.OVER_TIME, perTurn = 3, turns = 5))
    }

    @Test
    fun `should cap the healing at the maximum health when the heal exceeds the missing health`() {
        // Given
        val damagedTarget = battleUnit(unit = targetUnit.toDto())
        val damageEffect = decreaseHealthEffect(damage = 5, applicationType = "IMMEDIATELY").toDto()
        val damaged =
            damagedTarget
                .receiveImmediateEffect(damageEffect.id)
                .applyImmediateEffect(damageEffect, targetUnit.toDto(), 0, 0)
                .pullEvents()
                .second
        battleUnitRepository.create(damaged)
        val healEffect = increaseHealthEffect(healing = 20).toDto()
        val castAbility = givenAbility(cost = 0, effects = listOf(healEffect))
        givenApplications(row = 1, column = 1, applications = listOf(onUnit(damaged, healEffect)))
        // When
        val preview = previewAbilityCast(caster.id(), castAbility.id, listOf(PositionDto(1, 1)))
        // Then
        val targetPreview = preview.targets.single()
        assertThat(targetPreview.hpBefore).isEqualTo(15)
        assertThat(targetPreview.hpAfter).isEqualTo(20)
    }

    @Test
    fun `should predict the destination when the ability teleports the caster`() {
        // Given
        val effect = teleportEffect().toDto()
        val castAbility = givenAbility(cost = 5, effects = listOf(effect))
        givenApplications(row = 3, column = 4, applications = listOf(onUnit(caster, effect, destination = EffectTargetDto.Tile(row = 3, column = 4))))
        // When
        val preview = previewAbilityCast(caster.id(), castAbility.id, listOf(PositionDto(3, 4)))
        // Then
        assertThat(preview.targets.single().teleportTo).isEqualTo(PositionDto(row = 3, column = 4))
    }

    @Test
    fun `should predict the deployed unit when the ability deploys a battle unit on a tile`() {
        // Given
        val effect = deployBattleUnitEffect(unitId = "bee").toDto()
        val castAbility = givenAbility(cost = 10, effects = listOf(effect))
        givenApplications(row = 2, column = 3, applications = listOf(onTile(effect, row = 2, column = 3)))
        // When
        val preview = previewAbilityCast(caster.id(), castAbility.id, listOf(PositionDto(2, 3)))
        // Then
        assertThat(preview.targets).isEmpty()
        assertThat(preview.tiles).containsExactly(TilePreview(row = 2, column = 3, deploysUnitId = "bee"))
    }

    @Test
    fun `should list every target when the cast group has several positions`() {
        // Given
        val otherTarget = battleUnit(unit = targetUnit.toDto())
        battleUnitRepository.create(otherTarget)
        val venomEffect = decreaseHealthEffect(damage = 3).toDto()
        val castAbility = givenAbility(cost = 0, effects = listOf(venomEffect))
        givenApplications(row = 1, column = 2, applications = listOf(onUnit(target, venomEffect)))
        givenApplications(row = 1, column = 3, applications = listOf(onUnit(otherTarget, venomEffect)))
        // When
        val preview = previewAbilityCast(caster.id(), castAbility.id, listOf(PositionDto(1, 2), PositionDto(1, 3)))
        // Then
        assertThat(preview.targets.map { it.battleUnitId }).containsExactly(target.id(), otherTarget.id())
    }

    @Test
    fun `should predict the mana and the cooldown when the ability has a cost and a cooldown`() {
        // Given
        val damageEffect = decreaseHealthEffect(damage = 1, applicationType = "IMMEDIATELY").toDto()
        val castAbility = givenAbility(cost = 12, cooldown = 2, effects = listOf(damageEffect))
        givenApplications(row = 1, column = 2, applications = listOf(onUnit(target, damageEffect)))
        // When
        val preview = previewAbilityCast(caster.id(), castAbility.id, listOf(PositionDto(1, 2)))
        // Then
        assertThat(preview.manaBefore).isEqualTo(30)
        assertThat(preview.manaAfter).isEqualTo(18)
        assertThat(preview.cooldownAfter).isEqualTo(2)
    }

    @Test
    fun `should list the effect as unsupported without failing when the effect can't be previewed`() {
        // Given
        val unsupportedEffect = deployBattleUnitEffect().toDto()
        val castAbility = givenAbility(cost = 0, effects = listOf(unsupportedEffect))
        givenApplications(row = 1, column = 2, applications = listOf(onUnit(target, unsupportedEffect)))
        // When
        val preview = previewAbilityCast(caster.id(), castAbility.id, listOf(PositionDto(1, 2)))
        // Then
        assertThat(preview.unsupported).hasSize(1)
        assertThat(preview.unsupported.single()).contains(unsupportedEffect.id)
        assertThat(preview.targets).isEmpty()
    }

    @Test
    fun `should leave the stored battle units untouched when the preview is computed`() {
        // Given
        val damageEffect = decreaseHealthEffect(damage = 10, applicationType = "IMMEDIATELY").toDto()
        val castAbility = givenAbility(cost = 5, effects = listOf(damageEffect))
        givenApplications(row = 1, column = 2, applications = listOf(onUnit(target, damageEffect)))
        // When
        previewAbilityCast(caster.id(), castAbility.id, listOf(PositionDto(1, 2)))
        // Then
        assertThat(battleUnitRepository.searchById(target.id())).isEqualTo(target)
        assertThat(battleUnitRepository.searchById(caster.id())).isEqualTo(caster)
    }

    private fun givenAbility(
        cost: Int,
        cooldown: Int = 0,
        effects: List<Effect.Dto>,
    ): Ability.Dto {
        val dto = ability(cost = cost, cooldown = cooldown).toDto()
        whenever(searchAbilityById(dto.id)).thenReturn(dto)
        effects.forEach { whenever(searchEffectById(it.id)).thenReturn(it) }
        return dto
    }

    private fun givenApplications(
        row: Int,
        column: Int,
        applications: List<EffectApplicationDto>,
    ) {
        whenever(abilityExecution(eq(caster.id()), any(), eq(row), eq(column))).thenReturn(applications)
    }

    private fun onUnit(
        battleUnit: BattleUnit,
        effect: Effect.Dto,
        destination: EffectTargetDto? = null,
    ) = EffectApplicationDto(
        source = ApplicationSourceDto.battleUnit(caster.id()),
        target = EffectTargetDto.Unit(id = battleUnit.id()),
        effectId = effect.id,
        destination = destination,
    )

    private fun onTile(
        effect: Effect.Dto,
        row: Int,
        column: Int,
    ) = EffectApplicationDto(
        source = ApplicationSourceDto.battleUnit(caster.id()),
        target = EffectTargetDto.Tile(row = row, column = column),
        effectId = effect.id,
    )

    private fun BattleUnit.id() = toDto().id
}
