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
import com.mkz.rpg.effect.domain.EffectMother.decreaseHealthEffect
import com.mkz.rpg.effect.domain.EffectMother.increaseHealthEffect
import com.mkz.rpg.effect.domain.EffectMother.increaseManaEffect
import com.mkz.rpg.player.domain.PlayerMother.player
import com.mkz.rpg.shared.usecases.InMemoryQueries
import com.mkz.rpg.unit.domain.UnitMother.unit
import io.kotest.matchers.maps.shouldBeEmpty
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class PreviewCastRemainingHealthTest {
    private val queries = InMemoryQueries()
    private val previewCastRemainingHealth =
        PreviewCastRemainingHealth(
            searchAbilityById = queries.searchAbilityById,
            searchEffectById = queries.searchEffectById,
            battleUnitRepository = queries.battleUnitRepository,
            searchPosition = queries.searchPosition,
            abilityExecution = queries.abilityExecution,
        )

    /**
     * Stores the battle units standing on the battlefield map, with 20 health points, and the effects:
     * - `C` is the caster, of player-1, with 10 health points left. `E` and `F` are enemy-1 and enemy-2, of player-2.
     * - "slash" deals 6 damage and "smash" 30 immediately, "cure" heals 4 and "elixir" 50 immediately, "venom" deals
     *   3 damage on every turn started, and "ether" restores 5 mana immediately.
     */
    private fun givenBattle(map: String) {
        val player = player(id = "player-1").toDto()
        val enemyPlayer = player(id = "player-2").toDto()
        val unit = unit(healthPoints = 20).toDto()
        queries.battleUnitRepository.create(battleUnit(id = "caster", unit = unit, player = player, remainingHealthPoints = 10))
        queries.battleUnitRepository.create(battleUnit(id = "enemy-1", unit = unit, player = enemyPlayer))
        queries.battleUnitRepository.create(battleUnit(id = "enemy-2", unit = unit, player = enemyPlayer))
        val occupants = mapOf('C' to "caster", 'E' to "enemy-1", 'F' to "enemy-2").filterKeys { letter -> letter in map }
        queries.battlefieldRepository.create(battlefield(map, occupants = occupants))
        listOf(
            decreaseHealthEffect(id = "slash", damage = 6, applicationType = "IMMEDIATELY"),
            decreaseHealthEffect(id = "smash", damage = 30, applicationType = "IMMEDIATELY"),
            increaseHealthEffect(id = "cure", healing = 4),
            increaseHealthEffect(id = "elixir", healing = 50),
            decreaseHealthEffect(id = "venom", damage = 3, applicationType = "ON_TURN_STARTED"),
            increaseManaEffect(id = "ether", mana = 5),
        ).forEach { effect -> queries.effectRepository.create(Effect.create(effect.toDto())) }
    }

    private fun givenAbility(
        targeting: TargetingDto,
        vararg effectSpecs: Pair<String, TargetExpressionDto.Type>,
    ) {
        queries.abilityRepository.create(
            ability(id = "ability-1", targeting = targeting, effectSpecs = effectSpecs.map { (effectId, target) -> effectSpec(effectId = effectId, target = target) }),
        )
    }

    private fun preview(vararg castGroup: PositionDto) = previewCastRemainingHealth(battleUnitId = "caster", abilityId = "ability-1", castGroup = castGroup.toList())

    @Test
    fun `should subtract the damage dealt immediately when the ability is cast on the target`() {
        // Given
        givenBattle("C E")
        givenAbility(TargetingDto.ADJACENT_ENEMY, "slash" to SELECTED_TARGET, "slash" to SELECTED_TARGET)
        // When
        val remainingHealth = preview(PositionDto(0, 1))
        // Then
        remainingHealth shouldBe mapOf("enemy-1" to 8)
    }

    @Test
    fun `should not leave less than no health when the damage is higher than the health left`() {
        // Given
        givenBattle("C E")
        givenAbility(TargetingDto.ADJACENT_ENEMY, "smash" to SELECTED_TARGET)
        // When
        val remainingHealth = preview(PositionDto(0, 1))
        // Then
        remainingHealth shouldBe mapOf("enemy-1" to 0)
    }

    @Test
    fun `should add the healing when the ability heals the caster`() {
        // Given
        givenBattle("C")
        givenAbility(TargetingDto.SELF, "cure" to CASTER)
        // When
        val remainingHealth = preview(PositionDto(0, 0))
        // Then
        remainingHealth shouldBe mapOf("caster" to 14)
    }

    @Test
    fun `should not heal over the maximum health when the healing is higher than the health lost`() {
        // Given
        givenBattle("C")
        givenAbility(TargetingDto.SELF, "elixir" to CASTER)
        // When
        val remainingHealth = preview(PositionDto(0, 0))
        // Then
        remainingHealth shouldBe mapOf("caster" to 20)
    }

    @Test
    fun `should list the health of each target when the ability is cast on a cast group of several tiles`() {
        // Given
        givenBattle("C E\nF .")
        givenAbility(TargetingDto.ALL_ADJACENT_ENEMIES, "slash" to SELECTED_TARGET)
        // When
        val remainingHealth = preview(PositionDto(0, 1), PositionDto(1, 0))
        // Then
        remainingHealth shouldBe mapOf("enemy-1" to 14, "enemy-2" to 14)
    }

    @Test
    fun `should not list the battle units whose health the cast does not change immediately`() {
        // Given
        givenBattle("C E")
        givenAbility(TargetingDto.ADJACENT_ENEMY, "venom" to SELECTED_TARGET, "ether" to SELECTED_TARGET)
        // When
        val remainingHealth = preview(PositionDto(0, 1))
        // Then
        remainingHealth.shouldBeEmpty()
    }

    @Test
    fun `should not list any battle unit when the ability does not exist`() {
        // Given
        givenBattle("C E")
        // When
        val remainingHealth = previewCastRemainingHealth(battleUnitId = "caster", abilityId = "unknown-ability", castGroup = listOf(PositionDto(0, 1)))
        // Then
        remainingHealth.shouldBeEmpty()
    }
}
