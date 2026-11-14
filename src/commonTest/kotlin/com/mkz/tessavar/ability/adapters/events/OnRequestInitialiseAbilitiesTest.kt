package com.mkz.tessavar.ability.adapters.events

import com.mkz.tessavar.ability.adapters.storage.InMemoryAbilityRepository
import com.mkz.tessavar.ability.domain.AbilityEvent
import com.mkz.tessavar.ability.domain.AbilityMother.ability
import com.mkz.tessavar.ability.domain.AbilityMother.abilityEffect
import com.mkz.tessavar.ability.usecases.commands.InitialiseAbilities
import com.mkz.tessavar.ability.usecases.services.AbilityCreation
import com.mkz.tessavar.ability.usecases.services.FakeAbilityLoader
import com.mkz.tessavar.effect.adapters.storage.InMemoryEffectRepository
import com.mkz.tessavar.effect.domain.EffectMother.damageEffect
import com.mkz.tessavar.effect.usecases.queries.SearchEffectById
import com.mkz.tessavar.shared.adapters.events.InMemoryEventBus
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class OnRequestInitialiseAbilitiesTest {
    private val eventBus = InMemoryEventBus()
    private val abilityRepository = InMemoryAbilityRepository()
    private val effectRepository = InMemoryEffectRepository().also { it.create(damageEffect(id = "damage")) }
    private val sword = ability(id = "sword", abilityEffects = listOf(abilityEffect(effectId = "damage"))).toDto()
    private val onRequestInitialiseAbilities =
        OnRequestInitialiseAbilities(
            InitialiseAbilities(FakeAbilityLoader(abilities = listOf(sword)), AbilityCreation(abilityRepository, SearchEffectById(effectRepository)), eventBus),
            eventBus,
        )

    @Test
    fun `should initialise the abilities when the request is dispatched`() {
        // Given
        eventBus.publish(AbilityEvent.RequestInitialiseAbilities)
        // When
        eventBus.dispatch()
        // Then
        abilityRepository.searchById("sword")?.toDto() shouldBe sword
    }
}
