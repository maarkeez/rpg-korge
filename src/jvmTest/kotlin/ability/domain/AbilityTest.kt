package ability.domain

import ability.domain.AbilityError.AbilityCooldownAboveLimit
import ability.domain.AbilityError.AbilityCostAboveLimit
import ability.domain.AbilityError.AbilityEffectsAboveLimit
import ability.domain.AbilityError.AbilityEmptyEffects
import ability.domain.AbilityError.AbilityNameTooLong
import ability.domain.AbilityError.EmptyAbilityId
import ability.domain.AbilityError.EmptyAbilityName
import ability.domain.AbilityError.NegativeAbilityCooldown
import ability.domain.AbilityError.NegativeAbilityCost
import ability.domain.AbilityMother.ability
import org.assertj.core.api.Assertions.assertThat
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
            assertThat(createdAbility.toDto()).isEqualTo(dto)
        }

        @Test
        fun `should fail when the id is blank`() {
            // Given
            val dto = ability().toDto().copy(id = "  ")
            // When
            val createdAbility = runCatching { Ability.create(dto = dto) }
            // Then
            assertThat(createdAbility.exceptionOrNull()).isExactlyInstanceOf(EmptyAbilityId::class.java)
        }

        @Test
        fun `should fail when the name is blank`() {
            // Given
            val dto = ability().toDto().copy(name = "  ")
            // When
            val createdAbility = runCatching { Ability.create(dto = dto) }
            // Then
            assertThat(createdAbility.exceptionOrNull()).isExactlyInstanceOf(EmptyAbilityName::class.java)
        }

        @Test
        fun `should fail when the name is longer than 50 characters`() {
            // Given
            val dto = ability().toDto().copy(name = "a".repeat(51))
            // When
            val createdAbility = runCatching { Ability.create(dto = dto) }
            // Then
            assertThat(createdAbility.exceptionOrNull()).isExactlyInstanceOf(AbilityNameTooLong::class.java)
        }

        @Test
        fun `should fail when the cost is negative`() {
            // Given
            val dto = ability().toDto().copy(cost = -1)
            // When
            val createdAbility = runCatching { Ability.create(dto = dto) }
            // Then
            assertThat(createdAbility.exceptionOrNull()).isExactlyInstanceOf(NegativeAbilityCost::class.java)
        }

        @Test
        fun `should fail when the cost is above the limit`() {
            // Given
            val dto = ability().toDto().copy(cost = 1000)
            // When
            val createdAbility = runCatching { Ability.create(dto = dto) }
            // Then
            assertThat(createdAbility.exceptionOrNull()).isExactlyInstanceOf(AbilityCostAboveLimit::class.java)
        }

        @Test
        fun `should fail when the cooldown is negative`() {
            // Given
            val dto = ability().toDto().copy(cooldown = -1)
            // When
            val createdAbility = runCatching { Ability.create(dto = dto) }
            // Then
            assertThat(createdAbility.exceptionOrNull()).isExactlyInstanceOf(NegativeAbilityCooldown::class.java)
        }

        @Test
        fun `should fail when the cooldown is above the limit`() {
            // Given
            val dto = ability().toDto().copy(cooldown = 100)
            // When
            val createdAbility = runCatching { Ability.create(dto = dto) }
            // Then
            assertThat(createdAbility.exceptionOrNull()).isExactlyInstanceOf(AbilityCooldownAboveLimit::class.java)
        }

        @Test
        fun `should fail when the effects are empty`() {
            // Given
            val dto = ability().toDto().copy(effects = emptyList())
            // When
            val createdAbility = runCatching { Ability.create(dto = dto) }
            // Then
            assertThat(createdAbility.exceptionOrNull()).isExactlyInstanceOf(AbilityEmptyEffects::class.java)
        }

        @Test
        fun `should fail when the effects are above the limit`() {
            // Given
            val dto = ability().toDto().copy(effects = List(4) { "effect-$it" })
            // When
            val createdAbility = runCatching { Ability.create(dto = dto) }
            // Then
            assertThat(createdAbility.exceptionOrNull()).isExactlyInstanceOf(AbilityEffectsAboveLimit::class.java)
        }
    }

    @Nested
    inner class ToDto {
        @Test
        fun `should expose the ability data when converted to dto`() {
            // Given
            val dto = ability().toDto()
            val createdAbility = Ability.create(dto = dto)
            // When
            val result = createdAbility.toDto()
            // Then
            assertThat(result).isEqualTo(dto)
        }

        @Test
        fun `should expose the target pattern as its dto representation`() {
            // Given
            val dto = ability(targetPattern = Ability.Dto.TargetPatternDto.SELF).toDto()
            val createdAbility = Ability.create(dto = dto)
            // When
            val result = createdAbility.toDto()
            // Then
            assertThat(result.targetPattern).isEqualTo(Ability.Dto.TargetPatternDto.SELF)
        }
    }

    @Nested
    inner class PullEvents {
        @Test
        fun `should pull the created event when the ability has pending events`() {
            // Given
            val createdAbility = ability()
            // When
            val (events, _) = createdAbility.pullEvents()
            // Then
            assertThat(events).containsExactly(AbilityEvent.AbilityCreated(abilityId = createdAbility.toDto().id))
        }

        @Test
        fun `should not pull events when there are no pending events`() {
            // Given
            val (events, updatedAbility) = ability().pullEvents()
            // When
            val (pulledEvents, _) = updatedAbility.pullEvents()
            // Then
            assertThat(pulledEvents).isEmpty()
        }
    }
}
