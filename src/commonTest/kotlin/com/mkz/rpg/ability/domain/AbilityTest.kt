package com.mkz.rpg.ability.domain

import com.mkz.rpg.ability.domain.AbilityError.AbilityCooldownAboveLimit
import com.mkz.rpg.ability.domain.AbilityError.AbilityCostAboveLimit
import com.mkz.rpg.ability.domain.AbilityError.AbilityEffectsAboveLimit
import com.mkz.rpg.ability.domain.AbilityError.AbilityEmptyEffects
import com.mkz.rpg.ability.domain.AbilityError.AbilityTurnsDurationAboveLimit
import com.mkz.rpg.ability.domain.AbilityError.AbilityTurnsDurationBelowMinimum
import com.mkz.rpg.ability.domain.AbilityError.EmptyAbilityId
import com.mkz.rpg.ability.domain.AbilityError.NegativeAbilityCooldown
import com.mkz.rpg.ability.domain.AbilityError.NegativeAbilityCost
import com.mkz.rpg.ability.domain.AbilityMother.ability
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeTypeOf
import kotlin.test.Test

class AbilityTest {
    @Test
    fun `Create should create ability when the dto is valid`() {
        // Given
        val dto = ability().toDto()
        // When
        val createdAbility = Ability.create(dto = dto)
        // Then
        createdAbility.toDto() shouldBe dto
    }

    @Test
    fun `Create should fail when the id is blank`() {
        // Given
        val dto =
            ability()
                .toDto()
                .copy(id = "  ")
        // When
        val createdAbility = runCatching { Ability.create(dto = dto) }
        // Then
        createdAbility.exceptionOrNull().shouldBeTypeOf<EmptyAbilityId>()
    }

    @Test
    fun `Create should fail when the cost is negative`() {
        // Given
        val dto =
            ability()
                .toDto()
                .copy(cost = -1)
        // When
        val createdAbility = runCatching { Ability.create(dto = dto) }
        // Then
        createdAbility.exceptionOrNull().shouldBeTypeOf<NegativeAbilityCost>()
    }

    @Test
    fun `Create should create ability when the cost is zero`() {
        // Given
        val dto =
            ability()
                .toDto()
                .copy(cost = 0)
        // When
        val createdAbility = Ability.create(dto = dto)
        // Then
        createdAbility.toDto().cost shouldBe 0
    }

    @Test
    fun `Create should create ability when the cost is at the limit`() {
        // Given
        val dto =
            ability()
                .toDto()
                .copy(cost = 999)
        // When
        val createdAbility = Ability.create(dto = dto)
        // Then
        createdAbility.toDto().cost shouldBe 999
    }

    @Test
    fun `Create should fail when the cost is above the limit`() {
        // Given
        val dto =
            ability()
                .toDto()
                .copy(cost = 1000)
        // When
        val createdAbility = runCatching { Ability.create(dto = dto) }
        // Then
        createdAbility.exceptionOrNull().shouldBeTypeOf<AbilityCostAboveLimit>()
    }

    @Test
    fun `Create should create ability when the cooldown is zero`() {
        // Given
        val dto =
            ability()
                .toDto()
                .copy(cooldown = 0)
        // When
        val createdAbility = Ability.create(dto = dto)
        // Then
        createdAbility.toDto().cooldown shouldBe 0
    }

    @Test
    fun `Create should create ability when the cooldown is at the limit`() {
        // Given
        val dto =
            ability()
                .toDto()
                .copy(cooldown = 99)
        // When
        val createdAbility = Ability.create(dto = dto)
        // Then
        createdAbility.toDto().cooldown shouldBe 99
    }

    @Test
    fun `Create should fail when the cooldown is negative`() {
        // Given
        val dto =
            ability()
                .toDto()
                .copy(cooldown = -1)
        // When
        val createdAbility = runCatching { Ability.create(dto = dto) }
        // Then
        createdAbility.exceptionOrNull().shouldBeTypeOf<NegativeAbilityCooldown>()
    }

    @Test
    fun `Create should fail when the cooldown is above the limit`() {
        // Given
        val dto =
            ability()
                .toDto()
                .copy(cooldown = 100)
        // When
        val createdAbility = runCatching { Ability.create(dto = dto) }
        // Then
        createdAbility.exceptionOrNull().shouldBeTypeOf<AbilityCooldownAboveLimit>()
    }

    @Test
    fun `Create should create ability when the turns duration is at the minimum`() {
        // Given
        val dto =
            ability()
                .toDto()
                .copy(turnsDuration = 1)
        // When
        val createdAbility = Ability.create(dto = dto)
        // Then
        createdAbility.toDto().turnsDuration shouldBe 1
    }

    @Test
    fun `Create should create ability when the turns duration is at the limit`() {
        // Given
        val dto =
            ability()
                .toDto()
                .copy(turnsDuration = 99)
        // When
        val createdAbility = Ability.create(dto = dto)
        // Then
        createdAbility.toDto().turnsDuration shouldBe 99
    }

    @Test
    fun `Create should fail when the turns duration is below the minimum`() {
        // Given
        val dto =
            ability()
                .toDto()
                .copy(turnsDuration = 0)
        // When
        val createdAbility = runCatching { Ability.create(dto = dto) }
        // Then
        createdAbility.exceptionOrNull().shouldBeTypeOf<AbilityTurnsDurationBelowMinimum>()
    }

    @Test
    fun `Create should fail when the turns duration is above the limit`() {
        // Given
        val dto =
            ability()
                .toDto()
                .copy(turnsDuration = 100)
        // When
        val createdAbility = runCatching { Ability.create(dto = dto) }
        // Then
        createdAbility.exceptionOrNull().shouldBeTypeOf<AbilityTurnsDurationAboveLimit>()
    }

    @Test
    fun `Create should fail when the effects are empty`() {
        // Given
        val dto =
            ability()
                .toDto()
                .copy(effectSpecs = emptyList())
        // When
        val createdAbility = runCatching { Ability.create(dto = dto) }
        // Then
        createdAbility.exceptionOrNull().shouldBeTypeOf<AbilityEmptyEffects>()
    }

    @Test
    fun `Create should fail when the effects are above the limit`() {
        // Given
        val dto =
            ability()
                .toDto()
                .copy(effectSpecs = List(4) { "effect-$it" }.map { effectId -> AbilityMother.effectSpec(effectId = effectId) })
        // When
        val createdAbility = runCatching { Ability.create(dto = dto) }
        // Then
        createdAbility.exceptionOrNull().shouldBeTypeOf<AbilityEffectsAboveLimit>()
    }

    @Test
    fun `ToDto should expose the ability data when converted to dto`() {
        // Given
        val dto =
            ability()
                .toDto()
        val createdAbility = Ability.create(dto = dto)
        // When
        val result = createdAbility.toDto()
        // Then
        result shouldBe dto
    }

    @Test
    fun `ToDto should expose the targeting as its dto representation`() {
        // Given
        val dto =
            ability(targeting = Ability.Dto.TargetingDto.SELF)
                .toDto()
        val createdAbility = Ability.create(dto = dto)
        // When
        val result = createdAbility.toDto()
        // Then
        result.targeting shouldBe Ability.Dto.TargetingDto.SELF
    }

    @Test
    fun `PullEvents should pull the created event when the ability has pending events`() {
        // Given
        val createdAbility =
            ability()
        // When
        val (events, _) = createdAbility.pullEvents()
        // Then
        events.shouldContainExactly(AbilityEvent.AbilityCreated(abilityId = createdAbility.toDto().id))
    }

    @Test
    fun `PullEvents should not pull events when there are no pending events`() {
        // Given
        val (events, updatedAbility) =
            ability()
                .pullEvents()
        // When
        val (pulledEvents, _) = updatedAbility.pullEvents()
        // Then
        pulledEvents.shouldBeEmpty()
    }
}
