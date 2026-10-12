package com.mkz.rpg.ability.domain

import com.mkz.rpg.ability.domain.AbilityError.AbilityCooldownAboveLimit
import com.mkz.rpg.ability.domain.AbilityError.AbilityCostAboveLimit
import com.mkz.rpg.ability.domain.AbilityError.AbilityEffectsAboveLimit
import com.mkz.rpg.ability.domain.AbilityError.AbilityEmptyEffects
import com.mkz.rpg.ability.domain.AbilityError.AbilityNameTooLong
import com.mkz.rpg.ability.domain.AbilityError.EmptyAbilityId
import com.mkz.rpg.ability.domain.AbilityError.EmptyAbilityName
import com.mkz.rpg.ability.domain.AbilityError.NegativeAbilityCooldown
import com.mkz.rpg.ability.domain.AbilityError.NegativeAbilityCost
import com.mkz.rpg.ability.domain.AbilityMother.ability
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeTypeOf
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class AbilityTest {
    @Nested
    inner class Create {
        @Test
        fun `should create ability when the dto is valid`() {
            // Given
            val dto = ability().toDto()
            // When
            val createdAbility = Ability.create(dto = dto)
            // Then
            createdAbility.toDto() shouldBe dto
        }

        @Test
        fun `should fail when the id is blank`() {
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
        fun `should fail when the name is blank`() {
            // Given
            val dto =
                ability()
                    .toDto()
                    .copy(name = "  ")
            // When
            val createdAbility = runCatching { Ability.create(dto = dto) }
            // Then
            createdAbility.exceptionOrNull().shouldBeTypeOf<EmptyAbilityName>()
        }

        @Test
        fun `should create ability when the name has exactly 50 characters`() {
            // Given
            val dto =
                ability()
                    .toDto()
                    .copy(name = "a".repeat(50))
            // When
            val createdAbility = Ability.create(dto = dto)
            // Then
            createdAbility.toDto().name shouldBe "a".repeat(50)
        }

        @Test
        fun `should fail when the name is longer than 50 characters`() {
            // Given
            val dto =
                ability()
                    .toDto()
                    .copy(name = "a".repeat(51))
            // When
            val createdAbility = runCatching { Ability.create(dto = dto) }
            // Then
            createdAbility.exceptionOrNull().shouldBeTypeOf<AbilityNameTooLong>()
        }

        @Test
        fun `should fail when the cost is negative`() {
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
        fun `should create ability when the cost is zero`() {
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
        fun `should create ability when the cost is at the limit`() {
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
        fun `should fail when the cost is above the limit`() {
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
        fun `should create ability when the cooldown is zero`() {
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
        fun `should create ability when the cooldown is at the limit`() {
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
        fun `should fail when the cooldown is negative`() {
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
        fun `should fail when the cooldown is above the limit`() {
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
        fun `should fail when the effects are empty`() {
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
        fun `should fail when the effects are above the limit`() {
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
    }

    @Nested
    inner class ToDto {
        @Test
        fun `should expose the ability data when converted to dto`() {
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
        fun `should expose the targeting as its dto representation`() {
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
    }

    @Nested
    inner class PullEvents {
        @Test
        fun `should pull the created event when the ability has pending events`() {
            // Given
            val createdAbility =
                ability()
            // When
            val (events, _) = createdAbility.pullEvents()
            // Then
            events.shouldContainExactly(AbilityEvent.AbilityCreated(abilityId = createdAbility.toDto().id))
        }

        @Test
        fun `should not pull events when there are no pending events`() {
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
}
