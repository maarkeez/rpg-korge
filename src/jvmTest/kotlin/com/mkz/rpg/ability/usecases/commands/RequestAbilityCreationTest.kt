package com.mkz.rpg.ability.usecases.commands

import com.mkz.rpg.ability.adapters.storage.InMemoryAbilityRepository
import com.mkz.rpg.ability.domain.AbilityEvent
import com.mkz.rpg.ability.domain.AbilityMother.ability
import com.mkz.rpg.effect.domain.EffectMother.effect
import com.mkz.rpg.effect.usecases.queries.SearchEffectById
import com.mkz.rpg.shared.domain.FakeEventBus
import com.mkz.rpg.shared.domain.assertThat
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

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
        val effect = effect().toDto()
        val ability = ability(effects = listOf(effect.id)).toDto()
        whenever(searchEffectById(effect.id)).thenReturn(effect)
        // When
        requestAbilityCreation(abilityDto = ability)
        // Then
        val storedAbility = abilityRepository.searchById(ability.id)?.toDto()
        assertThat(storedAbility).isEqualTo(ability)
        assertThat(eventBus)
            .hasPublishedEvents(AbilityEvent.AbilityCreated(ability.id))
    }
}
