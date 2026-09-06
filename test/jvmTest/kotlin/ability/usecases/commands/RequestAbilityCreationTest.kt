package ability.usecases.commands

import ability.adapters.storage.*
import ability.domain.AbilityMother.ability
import effect.domain.*
import effect.usecases.queries.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.*
import org.mockito.kotlin.*
import shared.domain.*


class RequestAbilityCreationTest {
    private val abilityRepository = InMemoryAbilityRepository()
    private val eventBus: EventBus = mock()
    private val searchEffectById: SearchEffectById = mock()
    private val requestAbilityCreation = RequestAbilityCreation(
        abilityRepository = abilityRepository,
        searchEffectById = searchEffectById,
        eventBus = eventBus
    )

    @Test
    fun `should create ability`() {
        // Given
        val effect = EffectMother.effect().toDto()
        val ability = ability(
            effects = listOf(effect.id)
        ).toDto()
        whenever(searchEffectById(effect.id)).thenReturn(effect)
        // When
        requestAbilityCreation(abilityDto = ability)
        // Then
        val storedAbility = abilityRepository.searchById(ability.id)?.toDto()
        assertThat(storedAbility).isEqualTo(ability)
    }
}
