package com.mkz.rpg.battleUnit.usecases.queries

import com.mkz.rpg.ability.domain.Ability.Dto.TargetExpressionDto
import com.mkz.rpg.ability.domain.Ability.Dto.TargetExpressionDto.Type.CASTER
import com.mkz.rpg.ability.domain.Ability.Dto.TargetExpressionDto.Type.SELECTED_TARGET
import com.mkz.rpg.ability.domain.Ability.Dto.TargetingDto
import com.mkz.rpg.ability.domain.AbilityMother.ability
import com.mkz.rpg.ability.domain.AbilityMother.effectSpec
import com.mkz.rpg.battleUnit.domain.BattleUnitMother.battleUnit
import com.mkz.rpg.battlefield.domain.Battlefield.Dto.PositionDto
import com.mkz.rpg.battlefield.domain.BattlefieldMother.battlefield
import com.mkz.rpg.effect.domain.Effect
import com.mkz.rpg.effect.domain.EffectMother.applyEffectOnNearbyAlliesEffect
import com.mkz.rpg.effect.domain.EffectMother.decreaseHealthEffect
import com.mkz.rpg.player.domain.PlayerMother.player
import com.mkz.rpg.shared.usecases.InMemoryQueries
import io.kotest.matchers.maps.shouldBeEmpty
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class PreviewCastOngoingEffectsTest {
    private val queries = InMemoryQueries()
    private val previewCastOngoingEffects =
        PreviewCastOngoingEffects(
            searchAbilityById = queries.searchAbilityById,
            searchEffectById = queries.searchEffectById,
            abilityExecution = queries.abilityExecution,
        )

    /**
     * Stores the battle units standing on the battlefield map, and the effects:
     * - `C` is the caster, of player-1. `E` and `F` are enemy-1 and enemy-2, of player-2.
     * - "venom" is applied on every turn started, "curse" when defeated, and "slash" immediately.
     */
    private fun givenBattle(map: String) {
        val player = player(id = "player-1").toDto()
        val enemyPlayer = player(id = "player-2").toDto()
        val battleUnits = mapOf('C' to "caster", 'E' to "enemy-1", 'F' to "enemy-2")
        battleUnits.forEach { (letter, battleUnitId) ->
            queries.battleUnitRepository.create(battleUnit(id = battleUnitId, player = if (letter == 'C') player else enemyPlayer))
        }
        queries.battlefieldRepository.create(battlefield(map, occupants = battleUnits.filterKeys { letter -> letter in map }))
        queries.effectRepository.create(Effect.create(decreaseHealthEffect(id = "venom", applicationType = "ON_TURN_STARTED").toDto()))
        queries.effectRepository.create(Effect.create(applyEffectOnNearbyAlliesEffect(id = "curse", effectId = "venom").toDto()))
        queries.effectRepository.create(Effect.create(decreaseHealthEffect(id = "slash", applicationType = "IMMEDIATELY").toDto()))
    }

    private fun givenAbility(
        targeting: TargetingDto,
        vararg effectSpecs: Pair<String, TargetExpressionDto.Type>,
    ) {
        queries.abilityRepository.create(
            ability(id = "ability-1", targeting = targeting, effectSpecs = effectSpecs.map { (effectId, target) -> effectSpec(effectId = effectId, target = target) }),
        )
    }

    @Test
    fun `should list the effects that stay on the target when the ability is cast on it`() {
        // Given
        givenBattle("C E")
        givenAbility(TargetingDto.ADJACENT_ENEMY, "venom" to SELECTED_TARGET, "curse" to SELECTED_TARGET)
        // When
        val ongoingEffects = previewCastOngoingEffects(battleUnitId = "caster", abilityId = "ability-1", castGroup = listOf(PositionDto(0, 1)))
        // Then
        ongoingEffects shouldBe mapOf("enemy-1" to listOf("venom", "curse"))
    }

    @Test
    fun `should not list the effects applied immediately when the ability is cast`() {
        // Given
        givenBattle("C E")
        givenAbility(TargetingDto.ADJACENT_ENEMY, "slash" to SELECTED_TARGET)
        // When
        val ongoingEffects = previewCastOngoingEffects(battleUnitId = "caster", abilityId = "ability-1", castGroup = listOf(PositionDto(0, 1)))
        // Then
        ongoingEffects.shouldBeEmpty()
    }

    @Test
    fun `should list the effects of each target when the ability is cast on a cast group of several tiles`() {
        // Given
        givenBattle("C E\nF .")
        givenAbility(TargetingDto.ALL_ADJACENT_ENEMIES, "venom" to SELECTED_TARGET)
        // When
        val ongoingEffects =
            previewCastOngoingEffects(battleUnitId = "caster", abilityId = "ability-1", castGroup = listOf(PositionDto(0, 1), PositionDto(1, 0)))
        // Then
        ongoingEffects shouldBe mapOf("enemy-1" to listOf("venom"), "enemy-2" to listOf("venom"))
    }

    @Test
    fun `should list the caster effect once per tile when the ability is cast on a cast group of several tiles`() {
        // Given
        givenBattle("C E\nF .")
        givenAbility(TargetingDto.ALL_ADJACENT_ENEMIES, "venom" to CASTER)
        // When
        val ongoingEffects =
            previewCastOngoingEffects(battleUnitId = "caster", abilityId = "ability-1", castGroup = listOf(PositionDto(0, 1), PositionDto(1, 0)))
        // Then
        ongoingEffects shouldBe mapOf("caster" to listOf("venom", "venom"))
    }

    @Test
    fun `should not list any effect when the ability does not exist`() {
        // Given
        givenBattle("C E")
        // When
        val ongoingEffects = previewCastOngoingEffects(battleUnitId = "caster", abilityId = "unknown-ability", castGroup = listOf(PositionDto(0, 1)))
        // Then
        ongoingEffects.shouldBeEmpty()
    }
}
