package ability.adapters.storage

import ability.domain.AbilityMother
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class InMemoryAbilityRepositoryTest {
    private val abilityRepository = InMemoryAbilityRepository()

    @Nested
    inner class Create {
        @Test
        fun `should store the ability when the ability is created`() {
            // Given
            val ability = AbilityMother.ability()
            // When
            abilityRepository.create(ability)
            // Then
            val storedAbility = abilityRepository.searchById(ability.toDto().id)
            assertThat(storedAbility).isEqualTo(ability)
        }

        @Test
        fun `should replace the stored ability when an ability with the same id is created`() {
            // Given
            val ability = AbilityMother.ability(id = "ability-1")
            abilityRepository.create(ability)
            val updatedAbility = AbilityMother.ability(id = "ability-1", name = "Updated ability")
            // When
            abilityRepository.create(updatedAbility)
            // Then
            val storedAbility = abilityRepository.searchById("ability-1")
            assertThat(storedAbility).isEqualTo(updatedAbility)
        }
    }

    @Nested
    inner class SearchById {
        @Test
        fun `should return the ability when the ability is stored`() {
            // Given
            val ability = AbilityMother.ability()
            abilityRepository.create(ability)
            // When
            val storedAbility = abilityRepository.searchById(ability.toDto().id)
            // Then
            assertThat(storedAbility).isEqualTo(ability)
        }

        @Test
        fun `should return null when the ability is not stored`() {
            // When
            val storedAbility = abilityRepository.searchById("ability-unknown")
            // Then
            assertThat(storedAbility).isNull()
        }
    }
}
