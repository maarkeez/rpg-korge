package com.mkz.rpg.ability.adapters.events

import com.mkz.rpg.ability.adapters.storage.InMemoryAbilityRepository
import com.mkz.rpg.ability.domain.AbilityEvent
import com.mkz.rpg.ability.domain.AbilityMother.ability
import com.mkz.rpg.ability.domain.AbilityMother.effectSpec
import com.mkz.rpg.ability.usecases.commands.InitialiseAbilities
import com.mkz.rpg.ability.usecases.services.AbilityCreation
import com.mkz.rpg.ability.usecases.services.FakeAbilityLoader
import com.mkz.rpg.effect.adapters.storage.InMemoryEffectRepository
import com.mkz.rpg.effect.domain.EffectMother.decreaseHealthEffect
import com.mkz.rpg.effect.usecases.queries.SearchEffectById
import com.mkz.rpg.shared.adapters.events.InMemoryEventBus
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class OnRequestInitialiseAbilitiesTest {
    private val eventBus = InMemoryEventBus()
    private val abilityRepository = InMemoryAbilityRepository()
    private val effectRepository = InMemoryEffectRepository().also { it.create(decreaseHealthEffect(id = "damage")) }
    private val sword = ability(id = "sword", effectSpecs = listOf(effectSpec(effectId = "damage"))).toDto()
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
