package ability.usecases.queries

import ability.adapters.storage.*
import ability.domain.AbilityMother
import ability.domain.AbilityMother.ability
import org.assertj.core.api.Assertions.assertThat
import org.junit.*

class SearchAbilityByIdTest {
    private val abilityRepository = InMemoryAbilityRepository()
    private val searchAbilityById = SearchAbilityById(abilityRepository)

    @Test
    fun `should return ability when it exists`() {
        // Given
        val ability = ability()
        val abilityDto = ability.toDto()
        abilityRepository.create(ability)
        // When
        val storedAbility = searchAbilityById(abilityDto.id)
        // Then
        assertThat(storedAbility).isEqualTo(abilityDto)
    }

    @Test
    fun `should return null when ability does not exist`() {
        // Given
        val unknownAbilityId = AbilityMother.id()
        // When
        val storedAbility = searchAbilityById(unknownAbilityId)
        // Then
        assertThat(storedAbility).isNull()
    }
}
