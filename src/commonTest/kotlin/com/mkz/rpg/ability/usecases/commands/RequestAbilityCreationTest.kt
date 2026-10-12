package com.mkz.rpg.ability.usecases.commands

import com.mkz.rpg.ability.adapters.storage.InMemoryAbilityRepository
import com.mkz.rpg.ability.domain.AbilityEvent
import com.mkz.rpg.ability.domain.AbilityMother.ability
import com.mkz.rpg.ability.domain.AbilityMother.effectSpec
import com.mkz.rpg.effect.adapters.storage.InMemoryEffectRepository
import com.mkz.rpg.effect.domain.EffectMother.decreaseHealthEffect
import com.mkz.rpg.effect.usecases.queries.SearchEffectById
import com.mkz.rpg.shared.domain.FakeEventBus
import com.mkz.rpg.shared.domain.shouldHavePublishedEvents
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class RequestAbilityCreationTest {
    private val abilityRepository = InMemoryAbilityRepository()
    private val effectRepository = InMemoryEffectRepository()
    private val eventBus = FakeEventBus()
    private val requestAbilityCreation =
        RequestAbilityCreation(
            abilityRepository = abilityRepository,
            searchEffectById = SearchEffectById(effectRepository),
            eventBus = eventBus,
        )

    @Test
    fun `should create ability`() {
        // Given
        val effect = decreaseHealthEffect()
        effectRepository.create(effect)
        val ability = ability(effectSpecs = listOf(effectSpec(effectId = effect.toDto().id))).toDto()
        // When
        requestAbilityCreation(abilityDto = ability)
        // Then
        val storedAbility = abilityRepository.searchById(ability.id)?.toDto()
        storedAbility shouldBe ability
        eventBus.shouldHavePublishedEvents(AbilityEvent.AbilityCreated(ability.id))
    }
}
