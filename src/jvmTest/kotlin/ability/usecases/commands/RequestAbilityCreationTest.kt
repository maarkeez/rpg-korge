package ability.usecases.commands

import ability.adapters.storage.InMemoryAbilityRepository
import ability.domain.AbilityEvent
import ability.domain.AbilityMother.ability
import effect.domain.EffectMother
import effect.usecases.queries.SearchEffectById
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import shared.domain.FakeEventBus
import shared.domain.assertThat

class RequestAbilityCreationTest {
    private val abilityRepository = InMemoryAbilityRepository()
    private val eventBus = FakeEventBus()
    private val searchEffectById: SearchEffectById = mock()
    private val requestAbilityCreation =
        RequestAbilityCreation(
            abilityRepository = abilityRepository,
            searchEffectById = searchEffectById,
            eventBus = eventBus,
        )

    @Test
    fun `should create ability`() {
        // Given
        val effect = EffectMother.effect().toDto()
        val ability =
            ability(
                effects = listOf(effect.id),
            ).toDto()
        whenever(searchEffectById(effect.id)).thenReturn(effect)
        // When
        requestAbilityCreation(abilityDto = ability)
        // Then
        val storedAbility = abilityRepository.searchById(ability.id)?.toDto()
        assertThat(storedAbility).isEqualTo(ability)
        assertThat(eventBus).hasPublishedEvents(AbilityEvent.AbilityCreated(ability.id))
    }
}
