package com.mkz.rpg.ability.usecases.commands

import com.mkz.rpg.ability.adapters.storage.InMemoryAbilityRepository
import com.mkz.rpg.ability.domain.AbilityError.AbilityEffectDoesNotExist
import com.mkz.rpg.ability.domain.AbilityEvent.AbilityCreated
import com.mkz.rpg.ability.domain.AbilityMother.ability
import com.mkz.rpg.ability.domain.AbilityMother.effectSpec
import com.mkz.rpg.ability.usecases.services.AbilityLoader
import com.mkz.rpg.effect.domain.EffectMother.decreaseHealthEffect
import com.mkz.rpg.effect.usecases.queries.SearchEffectById
import com.mkz.rpg.shared.domain.FakeEventBus
import com.mkz.rpg.shared.domain.assertThat
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class InitialiseAbilitiesTest {
    private val abilityRepository = InMemoryAbilityRepository()
    private val eventBus = FakeEventBus()
    private val abilityLoader: AbilityLoader = mock()
    private val searchEffectById: SearchEffectById = mock()
    private val initialiseAbilities = InitialiseAbilities(abilityLoader, abilityRepository, searchEffectById, eventBus)

    @Test
    fun `should create every loaded ability when abilities are initialised`() {
        // Given
        val effect = decreaseHealthEffect(id = "damage").toDto()
        val sword = ability(id = "sword", effectSpecs = listOf(effectSpec(effectId = effect.id))).toDto()
        whenever(searchEffectById(effect.id)).thenReturn(effect)
        whenever(abilityLoader.loadAbilities()).thenReturn(listOf(sword))
        // When
        initialiseAbilities()
        // Then
        assertThat(abilityRepository.searchById("sword")?.toDto()).isEqualTo(sword)
        assertThat(eventBus).hasPublishedEvents(AbilityCreated("sword"))
    }

    @Test
    fun `should fail when an ability effect does not exist`() {
        // Given
        val sword = ability(id = "sword", effectSpecs = listOf(effectSpec(effectId = "missing-effect"))).toDto()
        whenever(abilityLoader.loadAbilities()).thenReturn(listOf(sword))
        // When
        val initialisation = { initialiseAbilities() }
        // Then
        assertThatThrownBy { initialisation() }.isInstanceOf(AbilityEffectDoesNotExist::class.java)
    }
}
